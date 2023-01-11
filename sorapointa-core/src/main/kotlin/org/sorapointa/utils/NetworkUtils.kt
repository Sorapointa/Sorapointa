package org.sorapointa.utils

import io.ktor.utils.io.core.*
import io.netty.buffer.ByteBuf
import mu.KotlinLogging
import org.sorapointa.proto.PacketHead
import org.sorapointa.proto.START_MAGIC
import org.sorapointa.proto.SoraPacket
import org.sorapointa.proto.readToSoraPacket
import org.sorapointa.utils.encoding.hex

private val logger = KotlinLogging.logger {}

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
