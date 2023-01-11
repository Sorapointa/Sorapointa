package org.sorapointa.server.network

import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import io.ktor.utils.io.core.*
import org.sorapointa.game.Avatar
import org.sorapointa.game.Player
import org.sorapointa.proto.PacketHead
import org.sorapointa.proto.writeSoraPacket

interface OutgoingPacket<T : Message<*, *>> {

    val cmdId: UShort
    var metadata: PacketHead?
    val adapter: ProtoAdapter<T>
    fun buildProto(): T
}

abstract class AbstractOutgoingPacket<T : Message<*, *>>(
    override val cmdId: UShort,
) : OutgoingPacket<T> {

    override var metadata: PacketHead? = null

    abstract override fun buildProto(): T
}

internal fun <T : Message<*, *>> OutgoingPacket<T>.toFinalBytePacket(): ByteArray {
    val packet = this
    return buildPacket {
        writeSoraPacket(packet.cmdId, packet.adapter, packet.buildProto(), packet.metadata)
    }.readBytes()
}

internal abstract class PlayerOutgoingPacket<T : Message<*, *>>(
    cmdId: UShort,
) : AbstractOutgoingPacket<T>(cmdId) {

    protected abstract val player: Player

    override fun buildProto(): T =
        with(player) {
            buildProto()
        }

    abstract fun Player.buildProto(): T
}

internal abstract class AvatarOutgoingPacket<T : Message<*, *>>(
    cmdId: UShort,
) : AbstractOutgoingPacket<T>(cmdId) {

    abstract val avatar: Avatar

    override fun buildProto(): T =
        with(avatar) {
            buildProto()
        }

    abstract fun Avatar.buildProto(): T
}
