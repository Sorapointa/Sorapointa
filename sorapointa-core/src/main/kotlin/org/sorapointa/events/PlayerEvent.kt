@file:Suppress("unused")

package org.sorapointa.events

import com.google.protobuf.GeneratedMessageV3
import org.sorapointa.event.AbstractEvent
import org.sorapointa.event.CancelableEvent
import org.sorapointa.game.Player
import org.sorapointa.server.network.NetworkHandler
import org.sorapointa.server.network.OutgoingPacket

abstract class PlayerEvent : AbstractEvent() {

    abstract val player: Player
}

class HandleIncomingPacketEvent<T : GeneratedMessageV3>(
    override val player: Player,
    val dataPacket: T
) : PlayerEvent(), CancelableEvent

internal abstract class NetworkEvent : AbstractEvent() {

    abstract val networkHandler: NetworkHandler
}

internal class HandlePreLoginIncomingPacketEvent<T : GeneratedMessageV3>(
    override val networkHandler: NetworkHandler,
    val dataPacket: T
) : NetworkEvent(), CancelableEvent

internal class SendOutgoingPacketEvent(
    override val networkHandler: NetworkHandler,
    val dataPacket: OutgoingPacket
) : NetworkEvent(), CancelableEvent
