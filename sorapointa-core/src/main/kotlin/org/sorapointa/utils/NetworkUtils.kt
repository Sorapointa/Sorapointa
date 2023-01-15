package org.sorapointa.utils

import io.ktor.utils.io.core.*
import io.netty.buffer.ByteBuf
import mu.KotlinLogging
import org.sorapointa.SorapointaConfig
import org.sorapointa.dataloader.common.EntityIdType
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
    return if (SorapointaConfig.data.debugSetting.camelCasePacketName) {
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
    if (this.readableBytes() > 12) { // 2+2+2+4+2 at least to be > 12 bytes]
        val before = this.toByteArray()
        val decrypted = if (logger.isDebugEnabled) {
            before.copyOf().xor(key)
        } else {
            before.xor(key)
        }
        val readPacket = decrypted.toReadPacket()
        if (decrypted.readUShort() == START_MAGIC) {
            block(readPacket.readToSoraPacket())
        } else {
            logger.debug {
                "Detected insanity packet, hex: ${before.hex} " +
                    "decrypted: ${readPacket.readBytes().hex} key: ${key.hex}"
            }
        }
    }
}

internal fun ByteArray.readUShort(): UShort {
    return (this[0].toUByte().toInt() shl 8 or this[1].toUByte().toInt()).toUShort()
}

internal fun Int.getNextGuid(uid: Int): Long {
    return (uid.toLong() shl 32) + this.toLong()
}

internal fun Int.getNextEntityId(idType: EntityIdType): Int {
    return (idType.value shl 24) + this
}
