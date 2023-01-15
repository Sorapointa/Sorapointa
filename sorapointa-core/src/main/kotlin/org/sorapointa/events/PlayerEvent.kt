@file:Suppress("unused")

package org.sorapointa.events

import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import org.sorapointa.event.AbstractEvent
import org.sorapointa.event.CancelableEvent
import org.sorapointa.game.Player
import org.sorapointa.proto.PacketHead
import org.sorapointa.server.network.NetworkHandler
import org.sorapointa.server.network.OutgoingPacket

abstract class PlayerEvent : AbstractEvent() {
    abstract val player: Player
}

// Login order: Init -> (FirstCreate) -> Login -> Disconnect

abstract class RecvPacketTriggerEvent : PlayerEvent() {
    abstract val metadata: PacketHead?

    fun sendPacket(packet: OutgoingPacket<*>) {
        player.sendPacket(packet, metadata)
    }

    suspend fun sendPacketSync(packet: OutgoingPacket<*>) {
        player.sendPacketSync(packet, metadata)
    }
}

class PlayerInitEvent(
    override val player: Player,
) : PlayerEvent()

class PlayerFirstCreateEvent(
    override val player: Player,
    override val metadata: PacketHead?,
    val pickAvatarId: Int,
) : RecvPacketTriggerEvent()

class PlayerLoginEvent(
    override val player: Player,
    override val metadata: PacketHead? = null, // metadata from PlayerLoginReq
) : RecvPacketTriggerEvent()

class PlayerDisconnectEvent(
    override val player: Player,
) : PlayerEvent()

internal abstract class NetworkEvent : AbstractEvent() {
    abstract val networkHandler: NetworkHandler
}

class HandlePacketEvent<T : Message<*, *>>(
    override val player: Player,
    val dataPacket: T,
    val metadata: PacketHead,
    val adapter: ProtoAdapter<T>,
) : PlayerEvent(), CancelableEvent

internal class HandlePreLoginPacketEvent<T : Message<*, *>>(
    override val networkHandler: NetworkHandler,
    val dataPacket: T,
    val metadata: PacketHead,
    val adapter: ProtoAdapter<T>,
) : NetworkEvent(), CancelableEvent

internal class SendPacketEvent<T : Message<*, *>>(
    override val networkHandler: NetworkHandler,
    val dataPacket: OutgoingPacket<T>,
) : NetworkEvent(), CancelableEvent
