@file:Suppress("unused")

package org.sorapointa.events

import com.google.protobuf.GeneratedMessageV3
import org.sorapointa.event.AbstractEvent
import org.sorapointa.event.CancelableEvent
import org.sorapointa.game.Player
import org.sorapointa.proto.SoraPacket
import org.sorapointa.server.network.OutgoingPacket

abstract class PlayerEvent : AbstractEvent() {

    abstract val player: Player
}

internal class HandleIncomingPacketEvent<T : GeneratedMessageV3>(
    override val player: Player,
    val dataPacket: T
) : PlayerEvent(), CancelableEvent

internal class HandleRawSoraPacketEvent(
    override val player: Player,
    val dataPacket: SoraPacket
) : PlayerEvent(), CancelableEvent

internal class SendOutgoingPacketEvent(
    override val player: Player,
    val dataPacket: OutgoingPacket
) : PlayerEvent(), CancelableEvent
