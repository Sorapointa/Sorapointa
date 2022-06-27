@file:Suppress("unused")

package org.sorapointa.utils

import io.ktor.utils.io.core.*

/**
 * Convert entire [UByteArray] to [ULong]
 *
 * @see splitToULongArray
 */
fun UByteArray.entireToULong(): ULong {
    require(this.size == 8) { "Size must be 8" }
    val uints = this.map { it.toULong() }
    return ((uints[7] and 0xFFuL) shl 56) or
        ((uints[6] and 0xFFuL) shl 48) or
        ((uints[5] and 0xFFuL) shl 40) or
        ((uints[4] and 0xFFuL) shl 32) or
        ((uints[3] and 0xFFuL) shl 24) or
        ((uints[2] and 0xFFuL) shl 16) or
        ((uints[1] and 0xFFuL) shl 8) or
        (uints[0] and 0xFFuL)
}

/**
 * [UByteArray] chunked with 8 bytes convert to [ULongArray]
 */
fun UByteArray.splitToULongArray() =
    chunked(8) {
        it.toUByteArray().entireToULong()
    }.toULongArray()

/**
 * Convert [ByteArray] to [ByteReadPacket]
 */
fun ByteArray.toReadPacket(): ByteReadPacket =
    buildPacket {
        writeFully(this@toReadPacket)
    }
