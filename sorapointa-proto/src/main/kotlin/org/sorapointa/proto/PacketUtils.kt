package org.sorapointa.proto

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessageV3
import io.ktor.utils.io.core.*
import org.sorapointa.proto.PacketHeadOuterClass.PacketHead
import org.sorapointa.utils.SorapointaInternal

/**
 * Sorapoint Datapacket
 *
 * @see readToSoraPacket
 * @see PacketId you could find all cmd id in there
 * @param cmdId packet id
 * @param metadata packet metadata,
 * in sorapointa-proto, it only could be read from protobuf,
 * so it could be empty but not null.
 * But it could be null if it is an outgoing datapacket in sorapointa-core.
 * @param data [ByteArray] inclued protobuf [GeneratedMessageV3]
 */
class SoraPacket(
    val cmdId: UShort,
    val metadata: PacketHead,
    val data: ByteArray
)

@SorapointaInternal fun ByteReadPacket.readToSoraPacket(): SoraPacket {
    discard(2) // Discard start magic
    val cmdId = readUShort()
    val metadataLength = readUShort()
    val dataLength = readUInt()
    val metadata = readBytes(metadataLength.toInt())
    val data = readBytes(dataLength.toInt())
    discard(2) // Discard end magic
    return SoraPacket(cmdId, PacketHead.parseFrom(metadata), data)
}

@SorapointaInternal fun BytePacketBuilder.writeSoraPacket(
    cmdId: UShort,
    data: GeneratedMessageV3,
    metadata: PacketHead? = null
) {
    val metadataBytes = metadata?.toByteArray() ?: byteArrayOf()
    val dataBytes = data.toByteArray()

    writeUShort(START_MAGIC)
    writeUShort(cmdId)
    writeUShort(metadataBytes.size.toUShort())
    writeUInt(dataBytes.size.toUInt())
    writeFully(metadataBytes)
    writeFully(dataBytes)
    writeUShort(END_MAGIC)
}

/**
 * Convert [ByteArray] to [ByteString] in protobuf
 */
fun ByteArray.toByteString(): ByteString =
    ByteString.copyFrom(this)
