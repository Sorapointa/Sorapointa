package org.sorapointa.game

import org.sorapointa.dataloader.common.ClimateType
import org.sorapointa.dataloader.def.SceneData
import org.sorapointa.dataloader.def.findSceneData
import org.sorapointa.events.PlayerLoginEvent
import org.sorapointa.game.data.DEFAULT_PEER_ID
import org.sorapointa.server.network.PlayerEnterSceneNotifyPacket
import org.sorapointa.utils.nowMilliseconds
import org.sorapointa.utils.qualifiedOrSimple
import java.util.concurrent.ConcurrentHashMap
import kotlin.contracts.contract

interface Scene {

    val owner: Player
    val id: Int
    val sceneData: SceneData

    val playerMap: Map<Player, Int>

    val entities: Map<Int, SceneEntity>

    val beginTime: Long
    var time: Int
    var climate: ClimateType

    fun addEntity(sceneEntity: SceneEntity)
}

class SceneImpl(
    override val owner: Player,
    override val id: Int,
) : Scene {

    override val sceneData: SceneData by lazy {
        findSceneData(id) ?: error("Could not find id: $id scene data")
    }

    override val entities: Map<Int, SceneEntity>
        get() = _entities.toMap()
    override val playerMap: Map<Player, Int>
        get() = _playerMap.toMap()

    // Player -> PeerId
    private val _playerMap = ConcurrentHashMap(mapOf(owner to DEFAULT_PEER_ID))

    // EntityId -> SceneEntity
    private val _entities = ConcurrentHashMap<Int, SceneEntity>()
    override val beginTime: Long = nowMilliseconds()
    override var time = 0
        set(value) { field = value % (24 * 60) }
    override var climate: ClimateType = ClimateType.CLIMATE_SUNNY

    internal fun init() {
        if (owner.avatarComp.curAvatarGuid != 0L) {
            addEntity(owner.avatarComp.getCurAvatar())
        }
        owner.registerEventListener<PlayerLoginEvent> {
            owner.impl().sendPacket(PlayerEnterSceneNotifyPacket.Login(owner))
        }
    }

    override fun addEntity(sceneEntity: SceneEntity) {
        _entities[sceneEntity.id] = sceneEntity
    }

    override fun toString(): String =
        "Scene[owner: $owner, playerMap: [$_playerMap], entities: $_entities]"
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Scene.impl(): SceneImpl {
    contract { returns() implies (this@impl is SceneImpl) }
    check(this is SceneImpl) {
        "A Scene instance is not instance of SceneImpl. Your instance: ${this::class.qualifiedOrSimple}"
    }
    return this
}
