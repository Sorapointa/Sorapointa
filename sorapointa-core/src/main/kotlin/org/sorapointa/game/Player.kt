package org.sorapointa.game

import com.squareup.wire.Message
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import mu.KotlinLogging
import org.sorapointa.Sorapointa
import org.sorapointa.command.CommandSender
import org.sorapointa.dataloader.common.EntityIdType
import org.sorapointa.dataloader.common.PlayerProp
import org.sorapointa.dispatch.data.Account
import org.sorapointa.event.InitStateController
import org.sorapointa.event.StateController
import org.sorapointa.event.WithState
import org.sorapointa.game.data.AvatarDataImpl
import org.sorapointa.game.data.CompoundAvatarTeamData
import org.sorapointa.game.data.PlayerData
import org.sorapointa.game.data.PlayerDataImpl
import org.sorapointa.proto.PacketHead
import org.sorapointa.proto.PropValue
import org.sorapointa.proto.SoraPacket
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

    val account: Account

    val data: PlayerData

    val uid: Int

    val scene: Scene

    val world: World

    val allAvatar: List<Avatar>

    val inventory: Inventory

    val guidEntityMap: MutableMap<Long, Int> // Guid -> EntityId

    val peerId: Int

    val enterSceneToken: Int

    val isMpModeAvailable: Boolean

    fun hasLoadedScene(id: Int, loadNow: Boolean = true): Boolean

    fun getNextEnterSceneToken(set: Boolean = true): Int

    fun getNextEntityId(idType: EntityIdType): Int

    fun getOrNextEntityId(idType: EntityIdType, guid: Long): Int
}

abstract class AbstractPlayer : Player {

    internal abstract val state:
        StateController<PlayerStateInterface.State, PlayerStateInterface, Player>

    internal abstract val sceneState:
        InitStateController<PlayerSceneStateInterface.State, PlayerSceneStateInterface, Player>

    internal abstract val playerProto: PlayerProto

    internal abstract fun <T : Message<*, *>> sendPacketAsync(
        packet: OutgoingPacket<T>,
        metadata: PacketHead? = null
    ): Job

    internal abstract suspend fun <T : Message<*, *>> sendPacket(
        packet: OutgoingPacket<T>,
        metadata: PacketHead? = null
    )

    internal abstract fun forwardHandlePacket(
        packet: SoraPacket
    ): Job

    internal abstract suspend fun close()

    internal abstract suspend fun init()
}

interface PlayerStateInterface : WithState<PlayerStateInterface.State> {
    enum class State {
        LOGIN,
        OK,
        CLOSED
    }
}

interface PlayerSceneStateInterface : WithState<PlayerSceneStateInterface.State> {

    enum class State {
        LOADING,
        INIT,
        LOADED
    }
}

class PlayerImpl internal constructor(
    override val account: Account,
    override val data: PlayerData,
    private val networkHandler: NetworkHandler,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : AbstractPlayer() {

    override val uid = account.id.value

    override var locale: Locale? = data.locale

    val scope = ModuleScope(toString(), parentCoroutineContext)

    override val state by lazy {
        StateController<PlayerStateInterface.State, PlayerStateInterface, Player>(
            scope = scope,
            parentStateClass = this,
            Login(this)
        )
    }

    override val sceneState by lazy {
        InitStateController<PlayerSceneStateInterface.State, PlayerSceneStateInterface, Player>(
            scope = scope,
            parentStateClass = this,
            SceneLoading(), SceneInit(), SceneLoaded()
        )
    }

    private val _scene = atomic(SceneImpl(this, data.sceneId))

    override val scene: Scene
        get() = _scene.value

    override val world = WorldImpl(this, scene)

    override val allAvatar
        get() = AvatarDataImpl.findAll(this)

    override val playerProto = PlayerProtoImpl(this)

    override val inventory = InventoryImpl(data, data.inventory)

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

    override suspend fun close() {
        networkHandler.close()
        state.setState(Closed())
        scope.dispose()
    }

    override suspend fun init() {
        logger.info { toString() + " has joined to the server" }
        if (data is PlayerDataImpl) data.player = this
        state.init()
        networkHandler.networkStateController.observeStateChange { _, state ->
            if (state == NetworkHandlerStateInterface.State.CLOSED) {
                onConnectionClosed()
            }
        }
    }

    internal fun initNewPlayerAvatar(pickInitAvatarId: Int) {
        val avatarGuid = data.getNextGuid()
        AvatarDataImpl.create(avatarGuid, this, pickInitAvatarId)
        data.compoundAvatarTeam = CompoundAvatarTeamData(avatarGuid)
        data.selectedAvatarGuid = avatarGuid
    }

    override suspend fun sendMessage(msg: String) {
    }

    override fun <T : Message<*, *>> sendPacketAsync(
        packet: OutgoingPacket<T>,
        metadata: PacketHead?,
    ): Job = networkHandler.sendPacketAsync(packet)

    override suspend fun <T : Message<*, *>> sendPacket(
        packet: OutgoingPacket<T>,
        metadata: PacketHead?,
    ) = networkHandler.sendPacket(packet)

    override fun forwardHandlePacket(
        packet: SoraPacket
    ) = networkHandler.handlePacket(packet)

    private fun onConnectionClosed() {
        logger.info { toString() + " has disconnected to the server" }
        Sorapointa.playerList.remove(this)
    }

    inner class Login(private val player: PlayerImpl) : PlayerStateInterface {

        override val state: PlayerStateInterface.State = PlayerStateInterface.State.LOGIN

        internal suspend fun onLogin() {
            // TODO: divide those into separate modules to implement `onLogin`
            val loginPackets = listOf(
                PlayerDataNotifyPacket(player),
                StoreWeightLimitNotifyPacket(),
                PlayerStoreNotifyPacket(player),
                AvatarDataNotifyPacket(player),
                OpenStateUpdateNotifyPacket(player),
                PlayerEnterSceneNotifyPacket.Login(player),
            )
            // TODO: Resin, Quest, Achievement, Activity,
            //  DailyTask, PlayerLevelReward,
            //  AvatarExpedition, AvatarSatiation,
            //  Cook, Combine (Craft), Forge,
            //  Investigation, Tower, Codex, Widget,
            //  Home, Announcement(Activity), Shop, Mail
            loginPackets.forEach {
                networkHandler.sendPacket(it)
            }

            sceneState.setState(PlayerSceneStateInterface.State.LOADING)
        }
    }

    inner class OK : PlayerStateInterface {

        override val state: PlayerStateInterface.State = PlayerStateInterface.State.OK
    }

    inner class Closed : PlayerStateInterface {

        override val state: PlayerStateInterface.State = PlayerStateInterface.State.CLOSED

        override suspend fun startState() {
            // onClosed
            data.lastActiveTime = now()
        }
    }

    inner class SceneLoading : PlayerSceneStateInterface {

        override val state: PlayerSceneStateInterface.State = PlayerSceneStateInterface.State.LOADING
    }

    inner class SceneInit : PlayerSceneStateInterface {

        override val state: PlayerSceneStateInterface.State = PlayerSceneStateInterface.State.INIT
    }

    inner class SceneLoaded : PlayerSceneStateInterface {

        override val state: PlayerSceneStateInterface.State = PlayerSceneStateInterface.State.LOADED
    }

    override fun toString(): String =
        "Player[id: $uid, host: ${networkHandler.host}]"
}

internal interface PlayerProto {

    val protoPropMap: Map<Int, PropValue>
}

class PlayerProtoImpl(
    private val player: Player
) : PlayerProto {

    override val protoPropMap
        get() = mapOf(
            PlayerProp.PROP_MAX_SPRING_VOLUME map player.data.maxSpringVolume,
            PlayerProp.PROP_CUR_SPRING_VOLUME map player.data.curSpringVolume,
            PlayerProp.PROP_IS_SPRING_AUTO_USE map player.data.isSpringAutoUse.toInt(),
            PlayerProp.PROP_SPRING_AUTO_USE_PERCENT map player.data.springAutoUsePercent,
            PlayerProp.PROP_IS_FLYABLE map player.data.isFlyable.toInt(),
            PlayerProp.PROP_IS_TRANSFERABLE map player.data.isTransferable.toInt(),
            PlayerProp.PROP_MAX_STAMINA map player.data.maxStamina,
            PlayerProp.PROP_CUR_PERSIST_STAMINA map player.data.curPersistStamina,
            PlayerProp.PROP_CUR_TEMPORARY_STAMINA map player.data.curTemporaryStamina,
            PlayerProp.PROP_PLAYER_LEVEL map player.data.playerLevel,
            PlayerProp.PROP_EXP map player.data.exp,
            PlayerProp.PROP_PLAYER_HCOIN map player.data.primoGem,
            PlayerProp.PROP_PLAYER_SCOIN map player.data.mora,
            PlayerProp.PROP_PLAYER_MP_SETTING_TYPE map player.data.mpSettingType.value,
            PlayerProp.PROP_IS_MP_MODE_AVAILABLE map player.data.isMpModeAvailable.toInt(),
            PlayerProp.PROP_PLAYER_WORLD_LEVEL map player.data.worldLevel,
            PlayerProp.PROP_PLAYER_RESIN map player.data.playerResin,
            PlayerProp.PROP_PLAYER_MCOIN map player.data.genesisCrystal,
            PlayerProp.PROP_PLAYER_LEGENDARY_KEY map player.data.legendaryStoryKey,
            PlayerProp.PROP_IS_HAS_FIRST_SHARE map player.data.isHasFirstShare.toInt(),
            PlayerProp.PROP_PLAYER_FORGE_POINT map player.data.playerForgePoint,
            PlayerProp.PROP_PLAYER_WORLD_LEVEL_ADJUST_CD map player.data.worldLevelAdjustCD,
            PlayerProp.PROP_PLAYER_LEGENDARY_DAILY_TASK_NUM map player.data.legendaryDailyTaskSum,
            PlayerProp.PROP_PLAYER_HOME_COIN map player.data.homeCoin,
        )
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Player.impl(): AbstractPlayer {
    contract { returns() implies (this@impl is AbstractPlayer) }
    check(this is AbstractPlayer) {
        "A Player instance is not instance of AbstractPlayer. Your instance: ${this::class.qualifiedOrSimple}"
    }
    return this
}
