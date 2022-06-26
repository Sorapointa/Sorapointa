package org.sorapointa.server.network

import com.google.protobuf.GeneratedMessageV3
import io.ktor.utils.io.core.*
import org.sorapointa.game.Avatar
import org.sorapointa.game.Player
import org.sorapointa.proto.PacketHeadOuterClass.PacketHead
import org.sorapointa.proto.writeSoraPacket

interface OutgoingPacket {

    val cmdId: UShort
    var metadata: PacketHead?

    fun buildProto(): GeneratedMessageV3
}

abstract class AbstractOutgoingPacket(
    override val cmdId: UShort,
) : OutgoingPacket {

    override var metadata: PacketHead? = null
}

internal fun OutgoingPacket.toFinalBytePacket(): ByteArray {
    val packet = this
    return buildPacket {
        writeSoraPacket(packet.cmdId, packet.buildProto(), packet.metadata)
    }.readBytes()
}

internal abstract class PlayerOutgoingPacket(
    cmdId: UShort
) : AbstractOutgoingPacket(cmdId) {

    protected abstract val player: Player

    override fun buildProto(): GeneratedMessageV3 =
        with(player) {
            buildProto()
        }

    abstract fun Player.buildProto(): GeneratedMessageV3
}

internal abstract class AvatarOutgoingPacket(
    cmdId: UShort
) : AbstractOutgoingPacket(cmdId) {

    abstract val avatar: Avatar

    override fun buildProto(): GeneratedMessageV3 =
        with(avatar) {
            buildProto()
        }

    abstract fun Avatar.buildProto(): GeneratedMessageV3
}
