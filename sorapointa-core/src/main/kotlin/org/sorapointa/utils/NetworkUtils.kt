package org.sorapointa.utils

import io.ktor.utils.io.core.*
import io.netty.buffer.ByteBuf
import mu.KotlinLogging
import org.sorapointa.SorapointaConfig
import org.sorapointa.proto.*
import org.sorapointa.utils.encoding.hex

private val logger = KotlinLogging.logger {}

/**
 * Find a common name for a packet id
 *
 * This method is **inefficient** due to reflection,
 * please call it in **performance insensitive situation**
 * or lazy load it.
 *
 * @param cmdId packet id
 */
fun findCommonNameFromCmdId(cmdId: UShort): String {
    val name = PacketId::class.java.declaredFields.first { it.get(PacketId).safeCast<Short>() == cmdId.toShort() }.name
    return if (SorapointaConfig.data.debugCamelCasePacketName) {
        name.toCamelCase()
    } else {
        name
    }
}

internal fun ByteBuf.toByteArray(): ByteArray {
    val bytes = ByteArray(this.readableBytes())
    this.readBytes(bytes)
    return bytes
}

internal fun ByteBuf.readToSoraPacket(
    key: ByteArray,
    block: (SoraPacket) -> Unit,
) {
    if (this.readableBytes() > 12) { // 2+2+2+4+2 at least to be > 12 bytes
        val before = this.toByteArray()
        val decrypt = before.xor(key).toReadPacket()
        if (decrypt.copy().readUShort() == START_MAGIC) {
            block(decrypt.readToSoraPacket())
        } else {
            logger.debug { "Detected insanity packet, hex: ${before.hex} decrypted: ${decrypt.readBytes().hex}" }
        }
    }
}

internal fun buildMetadata(sequenceId: Int) =
    PacketHead(
        client_sequence_id = sequenceId,
        sent_ms = nowMilliseconds(),
    )
