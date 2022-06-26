package org.sorapointa.game

import kotlinx.atomicfu.atomic
import org.sorapointa.dataloader.common.EntityIdType
import org.sorapointa.game.data.DEFAULT_PEER_ID
import java.util.concurrent.ConcurrentLinkedQueue

interface World {

    val owner: Player

    val sceneList: ConcurrentLinkedQueue<Scene>

    val playerList: Map<Player, Int>

    val hostPeerId: Int
        get() = playerList[owner] ?: error("Could not find world ${toString()} ownerPeerId")

    fun getNextEntityId(idType: EntityIdType): Int

}

class WorldImpl(
    override val owner: Player,
    initScene: Scene
) : World {

    override val sceneList: ConcurrentLinkedQueue<Scene> = ConcurrentLinkedQueue(listOf(initScene))

    override val playerList: Map<Player, Int>
        get() = sceneList
            .fold(mutableMapOf()) { acc, i -> acc.putAll(i.playerMap); acc }

    private var peerId = DEFAULT_PEER_ID

    fun getNextPeerId() = ++peerId

    private var nextEntityId by atomic(0)

    override fun getNextEntityId(idType: EntityIdType): Int {
        val nextId = ++nextEntityId
        return ((idType.value shl 24) + nextId)
    }

    override fun toString(): String =
        "World[owner: $owner, sceneList: [${sceneList.joinToString()}]]"

}
