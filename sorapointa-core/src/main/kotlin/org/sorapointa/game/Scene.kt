package org.sorapointa.game

import org.sorapointa.dataloader.common.ClimateType
import org.sorapointa.dataloader.def.SceneData
import org.sorapointa.dataloader.def.findSceneData
import org.sorapointa.game.data.DEFAULT_PEER_ID
import org.sorapointa.utils.nowMilliseconds
import java.util.concurrent.ConcurrentHashMap

interface Scene {

    val owner: Player
    val id: Int
    val sceneData: SceneData

    val playerMap: MutableMap<Player, Int>

    val entities: MutableMap<Long, SceneEntity>

    val beginTime: Long
    var time: Int
    var climate: ClimateType
}

class SceneImpl(
    override val owner: Player,
    override val id: Int,
) : Scene {

    override val sceneData: SceneData by lazy {
        findSceneData(id) ?: error("Could not find id: $id scene data")
    }

    // Player -> PeerId
    override val playerMap = ConcurrentHashMap(mapOf(owner to DEFAULT_PEER_ID))

    override val entities = ConcurrentHashMap<Long, SceneEntity>()
    override val beginTime: Long = nowMilliseconds()
    override var time = 0
        set(value) { field = value % (24 * 60) }
    override var climate: ClimateType = ClimateType.CLIMATE_SUNNY

    override fun toString(): String =
        "Scene[owner: $owner, playerMap: [$playerMap], entities: $entities]"
}
