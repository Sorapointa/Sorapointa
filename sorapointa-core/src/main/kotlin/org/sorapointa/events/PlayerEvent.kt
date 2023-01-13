@file:Suppress("unused")

package org.sorapointa.events

import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import org.sorapointa.event.AbstractEvent
import org.sorapointa.event.CancelableEvent
import org.sorapointa.game.Player
import org.sorapointa.server.network.NetworkHandler
import org.sorapointa.server.network.OutgoingPacket

abstract class PlayerEvent : AbstractEvent() {
    abstract val player: Player
}

class HandleIncomingPacketEvent<T : Message<*, *>>(
    override val player: Player,
    val dataPacket: T,
    val adapter: ProtoAdapter<T>,
) : PlayerEvent(), CancelableEvent

// Login order: Init -> (FirstCreate) -> Login -> Disconnect

class PlayerInitEvent(
    override val player: Player,
) : PlayerEvent()

class PlayerFirstCreateEvent(
    override val player: Player,
    val pickAvatarId: Int,
) : PlayerEvent()

class PlayerLoginEvent(
    override val player: Player,
) : PlayerEvent()

class PlayerDisconnectEvent(
    override val player: Player,
) : PlayerEvent()

internal abstract class NetworkEvent : AbstractEvent() {
    abstract val networkHandler: NetworkHandler
}

internal class HandlePreLoginIncomingPacketEvent<T : Message<*, *>>(
    override val networkHandler: NetworkHandler,
    val dataPacket: T,
    val adapter: ProtoAdapter<T>,
) : NetworkEvent(), CancelableEvent

internal class SendOutgoingPacketEvent<T : Message<*, *>>(
    override val networkHandler: NetworkHandler,
    val dataPacket: OutgoingPacket<T>,
) : NetworkEvent(), CancelableEvent
