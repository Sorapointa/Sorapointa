package org.sorapointa.proto

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessageV3
import io.ktor.utils.io.core.*
import org.sorapointa.proto.PacketHeadOuterClass.PacketHead
import org.sorapointa.utils.SorapointaInternal

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

fun ByteArray.toByteString(): ByteString =
    ByteString.copyFrom(this)

// @SorapointaInternal fun BytePacketBuilder.encryptPacket(key: ByteArray): ByteReadPacket {
//    val data = this.
//    return buildPacket {
//
//
//    }
// }

//
// abstract class SoraPacket(
//    private val cmdId: Int,
//    private val data: GeneratedMessageV3
// ) {
//
//    companion object {
//        private lateinit var dispatchKey: ByteArray
//        private lateinit var gameKey: ByteArray
//
//        fun initKey(dispatchKey: ByteArray, gameKey: ByteArray) {
//            this.dispatchKey = dispatchKey
//            this.gameKey = gameKey
//        }
//
//        fun initKey(dispatchKey: ByteArray) {
//            this.dispatchKey = dispatchKey
//        }
//
//        fun decode(byteBuf: ByteBuf, useDispatchKey: Boolean = false): SoraPacket {
//
//            val decryptedBuf = byteBuf.toByteArray().xor(if (useDispatchKey) dispatchKey else gameKey)
//
//
//            // Read cmdID -> Parser
//            // Read ...
//            // Read data ByteArray -> GeneratedMessageV3
//            // parser.parseFrom(ByteArray)
//
//            // SoraPacket
//
//
//        }
//
//    }
//
//    private var metadata: PacketHead? = null
//
//    fun setMetadata(clientSequence: Int = 0): SoraPacket {
//        metadata = packetHead {
//            clientSequenceId = clientSequence
//            timestamp = Clock.System.currentTimeMillis()
//        }
//
//        return this
//    }
//
//    /**
//     * Encode data packet with `dispatchKey` and `gameKey`
//     *
//     * Designed for server that hardcoded dispatch key
//     * We still don't know how to generate dispatch key, so only hardcode one.
//     *
//     * Designed for server that hardcoded game and dispatch key
//     */
//    fun encode(): ByteArray =
//        encode(dispatchKey, gameKey)
//
//    /**
//     * Encode data packet with `dispatchKey` and `gameKey`
//     *
//     * Designed for server that hardcoded dispatch key
//     * We still don't know how to generate dispatch key, so only hardcode one.
//     *
//     * @param gameKey
//     */
//    fun encode(gameKey: ByteArray): ByteArray =
//        encode(dispatchKey, gameKey)
//
//
//    /**
//     * Encode data packet with `dispatchKey` and `gameKey`
//     *
//     * @param dispatchKey
//     * @param gameKey
//     */
//    fun encode(dispatchKey: ByteArray, gameKey: ByteArray): ByteArray {
//        val metaBytes = metadata?.toByteArray() ?: byteArrayOf()
//        val dataBytes = data.toByteArray()
//
//        val stream = ByteArrayOutputStream(2 + 2 + 2 + 4 + metaBytes.size + dataBytes.size + 2)
//
//        stream.writeUint16(START_MAGIC)
//        stream.writeUint16(cmdId)
//        stream.writeUint16(metaBytes.size)
//        stream.writeUint32(dataBytes.size)
//        stream.writeBytes(metaBytes)
//        stream.writeBytes(dataBytes)
//        stream.writeUint16(END_MAGIC)
//
//        val packet = stream.toByteArray()
//
//        if (this is UseDispatchKey) {
//            packet.xor(dispatchKey)
//        } else {
//            packet.xor(gameKey)
//        }
//
//        return packet
//    }
//
// }
//
// // Unsigned short
// private fun ByteArrayOutputStream.writeUint16(i: Int) {
//    write((i ushr 8 and 0xFF).toByte().toInt())
//    write((i and 0xFF).toByte().toInt())
// }
//
// // Unsigned int (long)
// private fun ByteArrayOutputStream.writeUint32(i: Int) {
//    write((i ushr 24 and 0xFF).toByte().toInt())
//    write((i ushr 16 and 0xFF).toByte().toInt())
//    write((i ushr 8 and 0xFF).toByte().toInt())
//    write((i and 0xFF).toByte().toInt())
// }
//
// private fun ByteBuf.toByteArray(): ByteArray {
//    val bytes = ByteArray(this.capacity())
//    this.getBytes(0, bytes)
//    return bytes
// }
//
//
// internal fun ByteBuf.toReadPacket(): ByteReadPacket {
//    val buf = this
//    return buildPacket {
//        ByteBufInputStream(buf).withUse { copyTo(outputStream()) }
//    }
// }
//
