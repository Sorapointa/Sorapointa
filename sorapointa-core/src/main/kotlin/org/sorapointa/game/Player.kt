package org.sorapointa.game

import com.squareup.wire.Message
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import mu.KotlinLogging
import org.sorapointa.Sorapointa
import org.sorapointa.command.CommandSender
import org.sorapointa.dataloader.common.EntityIdType
import org.sorapointa.dataloader.common.PlayerProp
import org.sorapointa.event.*
import org.sorapointa.events.PlayerDisconnectEvent
import org.sorapointa.events.PlayerEvent
import org.sorapointa.events.PlayerInitEvent
import org.sorapointa.events.PlayerLoginEvent
import org.sorapointa.game.data.PlayerData
import org.sorapointa.proto.MpSettingType
import org.sorapointa.proto.PacketHead
import org.sorapointa.proto.PropValue
import org.sorapointa.proto.SoraPacket
import org.sorapointa.proto.bin.PlayerDataBin
import org.sorapointa.server.network.*
import org.sorapointa.utils.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

interface Player : CommandSender {

    override var locale: Locale?

    val state: StateController<PlayerStateInterface.State, PlayerStateInterface, Player>

    val uid: Int

    val scene: Scene

    val world: World

    val basicComp: PlayerBasicComp
    val avatarComp: PlayerAvatarComp
    val itemComp: PlayerItemComp
    val sceneComp: PlayerSceneComp
    val socialComp: PlayerSocialComp

    val guidEntityMap: MutableMap<Long, Int> // Guid -> EntityId

    val peerId: Int

    val enterSceneToken: Int

    val isMpModeAvailable: Boolean

    fun hasLoadedScene(id: Int, loadNow: Boolean = true): Boolean

    fun getNextEnterSceneToken(set: Boolean = true): Int

    fun getNextEntityId(idType: EntityIdType): Int

    fun getOrNextEntityId(idType: EntityIdType, guid: Long): Int

    fun <T : Message<*, *>> sendPacket(
        packet: OutgoingPacket<T>,
        metadata: PacketHead? = null,
    ): Job

    suspend fun <T : Message<*, *>> sendPacketSync(
        packet: OutgoingPacket<T>,
        metadata: PacketHead? = null,
    )

    fun forwardHandlePacket(packet: SoraPacket): Job

    suspend fun saveData()
}

interface PlayerStateInterface : WithState<PlayerStateInterface.State> {
    enum class State {
        LOGIN,
        OK,
        CLOSED,
    }
}

class PlayerImpl internal constructor(
    override val uid: Int,
    private val data: PlayerData,
    internal val networkHandler: NetworkHandler,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : Player {

    override var locale: Locale? = data.locale

    val scope = ModuleScope(toString(), parentCoroutineContext)

    override val state by lazy {
        StateController<PlayerStateInterface.State, PlayerStateInterface, Player>(
            scope = scope,
            parentStateClass = this,
            Login(this),
        )
    }

    private lateinit var dataBin: PlayerDataBin

    override val basicComp by lazy {
        PlayerBasicComp(this, dataBin.basic_bin ?: error("PlayerBasicComp is null"))
    }

    override val avatarComp by lazy {
        PlayerAvatarComp(this, dataBin.avatar_bin ?: error("PlayerAvatarComp is null"))
    }

    override val itemComp by lazy {
        PlayerItemComp(this, dataBin.item_bin ?: error("PlayerItemComp is null"))
    }

    override val sceneComp by lazy {
        PlayerSceneComp(this, dataBin.scene_bin ?: error("PlayerSceneComp is null"))
    }

    override val socialComp by lazy {
        PlayerSocialComp(this, dataBin.social_bin ?: error("PlayerSocialComp is null"))
    }

    override val scene: Scene
        get() = _scene.value

    private val _scene by lazy {
        atomic(SceneImpl(this, sceneComp.myCurSceneId))
    }

    override val world by lazy {
        WorldImpl(this, scene)
    }

    internal val playerProto by lazy {
        PlayerProtoImpl(this)
    }

    override val guidEntityMap: MutableMap<Long, Int> = ConcurrentHashMap()

    override val peerId: Int
        get() = scene.playerMap[this] ?: error("Could not find player peer id from scene $scene")

    private val _enterSceneToken = atomic(getNextEnterSceneToken(false))

    override val enterSceneToken: Int
        get() = _enterSceneToken.value

    private val loadedSceneList = ConcurrentLinkedDeque<Int>()

    private val _isMpModeAvailable = atomic(true)

    override val isMpModeAvailable: Boolean
        get() = _isMpModeAvailable.value

    override fun hasLoadedScene(id: Int, loadNow: Boolean): Boolean =
        loadedSceneList.contains(id).also {
            if (!it && loadNow) loadedSceneList.add(id)
        }

    override fun getNextEnterSceneToken(set: Boolean): Int =
        (1000..99999).random().also {
            if (set) _enterSceneToken.value = it
        }

    override fun getNextEntityId(idType: EntityIdType): Int =
        scene.owner.world.getNextEntityId(idType)

    override fun getOrNextEntityId(idType: EntityIdType, guid: Long): Int =
        guidEntityMap[guid] ?: getNextEntityId(idType).also { entityId ->
            guidEntityMap[guid] = entityId
        }

    internal suspend fun close() {
        networkHandler.close()
        state.setState(Closed(this))
        scope.dispose()
    }

    internal suspend fun init() {
        logger.info { toString() + " has joined to the server" }
        dataBin = data.getPlayerDataBin()
        state.init()
        networkHandler.state.observeStateChange { _, state ->
            if (state == NetworkHandlerStateI.State.CLOSED) {
                close()
            }
        }
        basicComp.init()
        avatarComp.init()
        itemComp.init()
        sceneComp.init()
        socialComp.init()
        scene.impl().init()
        data.player = this
        PlayerInitEvent(this).broadcast()
    }

    override suspend fun sendMessage(msg: String) {
    }

    override fun <T : Message<*, *>> sendPacket(
        packet: OutgoingPacket<T>,
        metadata: PacketHead?,
    ): Job = networkHandler.sendPacket(packet, metadata)

    override suspend fun <T : Message<*, *>> sendPacketSync(
        packet: OutgoingPacket<T>,
        metadata: PacketHead?,
    ) = networkHandler.sendPacketSync(packet, metadata)

    override fun forwardHandlePacket(
        packet: SoraPacket,
    ): Job = networkHandler.handlePacket(packet)

    override suspend fun saveData() {
        data.save()
    }

    inner class Login(private val player: PlayerImpl) : PlayerStateInterface {

        override val state: PlayerStateInterface.State = PlayerStateInterface.State.LOGIN

        internal suspend fun onLogin() {
            PlayerLoginEvent(player).broadcast()
            basicComp.updateLastLoginTime()
        }
    }

    inner class OK : PlayerStateInterface {

        override val state: PlayerStateInterface.State = PlayerStateInterface.State.OK
    }

    inner class Closed(private val player: PlayerImpl) : PlayerStateInterface {

        override val state: PlayerStateInterface.State = PlayerStateInterface.State.CLOSED

        override suspend fun startState() {
            // onClosed
            player.data.save()
            logger.info { toString() + " has disconnected to the server" }
            PlayerDisconnectEvent(player).broadcast()
            Sorapointa.playerList.remove(player)
            basicComp.updateLastLoginTime()
        }
    }

    override fun toString(): String =
        "Player[id: $uid, host: ${networkHandler.host}]"
}

internal interface PlayerProto {

    val protoPropMap: Map<Int, PropValue>
}

class PlayerProtoImpl(
    private val player: Player,
) : PlayerProto {

    override val protoPropMap
        get() = mapOf(
            PlayerProp.PROP_MAX_SPRING_VOLUME map 100, // TODO: hardcode
            PlayerProp.PROP_CUR_SPRING_VOLUME map player.avatarComp.curSpringVolume,
            PlayerProp.PROP_IS_SPRING_AUTO_USE map player.avatarComp.isSpringAutoUse.toInt(),
            PlayerProp.PROP_SPRING_AUTO_USE_PERCENT map player.avatarComp.springAutoUsePercent,
            PlayerProp.PROP_IS_FLYABLE map player.avatarComp.isFlyable.toInt(),
            PlayerProp.PROP_IS_TRANSFERABLE map player.avatarComp.isTransferable.toInt(),
            PlayerProp.PROP_MAX_STAMINA mapFloat player.basicComp.persistStaminaLimit,
            PlayerProp.PROP_CUR_PERSIST_STAMINA mapFloat player.basicComp.curPersistStamina,
            PlayerProp.PROP_CUR_TEMPORARY_STAMINA mapFloat player.basicComp.curTemporaryStamina,
            PlayerProp.PROP_PLAYER_LEVEL map player.basicComp.level,
            PlayerProp.PROP_EXP map player.basicComp.exp,
            PlayerProp.PROP_PLAYER_HCOIN map player.itemComp.primoGem,
            PlayerProp.PROP_PLAYER_SCOIN map player.itemComp.mora,
            PlayerProp.PROP_PLAYER_MP_SETTING_TYPE map
                MpSettingType.MP_SETTING_TYPE_ENTER_AFTER_APPLY.value, // TODO: hardcode
            PlayerProp.PROP_IS_MP_MODE_AVAILABLE map 1, // TODO: hardcode
            PlayerProp.PROP_PLAYER_WORLD_LEVEL map player.sceneComp.world.level,
            PlayerProp.PROP_PLAYER_RESIN map 160, // TODO: hardcode
            PlayerProp.PROP_PLAYER_MCOIN map player.itemComp.genesisCrystal,
            PlayerProp.PROP_PLAYER_LEGENDARY_KEY map player.itemComp.legendaryKey,
            PlayerProp.PROP_IS_HAS_FIRST_SHARE map player.socialComp.isHaveFirstShare.toInt(),
            PlayerProp.PROP_PLAYER_FORGE_POINT map 0, // TODO: hardcode
            PlayerProp.PROP_PLAYER_WORLD_LEVEL_ADJUST_CD map 0, // TODO: hardcode
            PlayerProp.PROP_PLAYER_LEGENDARY_DAILY_TASK_NUM map 0, // TODO: hardcode
            PlayerProp.PROP_PLAYER_HOME_COIN map player.itemComp.homeCoin,
        )
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Player.impl(): PlayerImpl {
    contract { returns() implies (this@impl is PlayerImpl) }
    check(this is PlayerImpl) {
        "A Player instance is not instance of PlayerImpl. Your instance: ${this::class.qualifiedOrSimple}"
    }
    return this
}

inline fun <reified T : PlayerEvent> Player.registerEventListener(
    noinline listener: suspend T.() -> Unit,
) {
    registerListener<T> {
        if (it.player == this) {
            listener(it)
        }
    }
}

inline fun <reified T : PlayerEvent> Player.registerEventListener(
    priority: EventPriority,
    noinline listener: suspend T.() -> Unit,
) {
    registerListener<T>(priority) {
        if (it.player == this) {
            listener(it)
        }
    }
}

inline fun <reified T : PlayerEvent> Player.registerEventBlockListener(
    noinline listener: suspend T.() -> Unit,
) {
    registerBlockListener<T> {
        if (it.player == this) {
            listener(it)
        }
    }
}

inline fun <reified T : PlayerEvent> Player.registerEventBlockListener(
    priority: EventPriority,
    noinline listener: suspend T.() -> Unit,
) {
    registerBlockListener<T>(priority) {
        if (it.player == this) {
            listener(it)
        }
    }
}
