package org.sorapointa.proto

import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import io.ktor.utils.io.core.*
import org.sorapointa.utils.SorapointaInternal

/**
 * Sorapointa Datapacket
 *
 * @see readToSoraPacket
 * @see PacketId you could find all cmd id in there
 * @param cmdId packet id
 * @param metadata packet metadata,
 * in sorapointa-proto, it only could be read from protobuf,
 * so it could be empty but not null.
 * But it could be null if it is an outgoing datapacket in sorapointa-core.
 * @param data [ByteArray] inclued protobuf [Message]
 */
class SoraPacket(
    val cmdId: UShort,
    val metadata: PacketHead,
    val data: ByteArray,
)

@SorapointaInternal
fun ByteReadPacket.readToSoraPacket(): SoraPacket {
    discard(2) // Discard start magic
    val cmdId = readUShort()
    val metadataLength = readUShort()
    val dataLength = readUInt()
    val metadata = readBytes(metadataLength.toInt())
    val data = readBytes(dataLength.toInt())
    discard(2) // Discard end magic
    return SoraPacket(cmdId, PacketHead.ADAPTER.decode(metadata), data)
}

@SorapointaInternal
fun <T : Message<*, *>, E : ProtoAdapter<T>> BytePacketBuilder.writeSoraPacket(
    cmdId: UShort,
    adapter: E,
    data: T? = null,
    metadata: PacketHead? = null,
) {
    val metadataBytes = metadata?.let { PacketHead.ADAPTER.encode(it) } ?: byteArrayOf()
    val dataBytes = data?.let { adapter.encode(it) } ?: byteArrayOf()

    writeUShort(START_MAGIC)
    writeUShort(cmdId)
    writeUShort(metadataBytes.size.toUShort())
    writeUInt(dataBytes.size.toUInt())
    writeFully(metadataBytes)
    writeFully(dataBytes)
    writeUShort(END_MAGIC)
}
