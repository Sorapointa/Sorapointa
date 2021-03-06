@file:Suppress("unused")

package org.sorapointa.utils

/**
 * Author: HolographicHat
 */

fun Int.toByteArray(littleEndian: Boolean = true) = ByteArray(4).also { a ->
    a[0] = shr(if (littleEndian) 0 else 24).toByte()
    a[1] = shr(if (littleEndian) 8 else 16).toByte()
    a[2] = shr(if (littleEndian) 16 else 8).toByte()
    a[3] = shr(if (littleEndian) 24 else 0).toByte()
}

fun UInt.toByteArray(littleEndian: Boolean = true) = toInt().toByteArray(littleEndian)

fun Long.toByteArray(littleEndian: Boolean = true) = ByteArray(8).also { a ->
    a[0] = shr(if (littleEndian) 0 else 56).toByte()
    a[1] = shr(if (littleEndian) 8 else 48).toByte()
    a[2] = shr(if (littleEndian) 16 else 40).toByte()
    a[3] = shr(if (littleEndian) 24 else 32).toByte()
    a[4] = shr(if (littleEndian) 32 else 24).toByte()
    a[5] = shr(if (littleEndian) 40 else 16).toByte()
    a[6] = shr(if (littleEndian) 48 else 8).toByte()
    a[7] = shr(if (littleEndian) 56 else 0).toByte()
}

fun ULong.toByteArray() = toLong().toByteArray()

fun ByteArray.xor(key: ByteArray): ByteArray {
    for (i in this.indices) {
        this[i] = (this[i].toInt() xor key[i % key.size].toInt()).toByte()
    }
    return this
}

fun UByteArray.xor(key: UByteArray): UByteArray {
    for (i in this.indices) {
        this[i] = this[i] xor key[i % key.size]
    }
    return this
}

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

fun UByteArray.splitToULongArray() =
    chunked(8) {
        it.toUByteArray().entireToULong()
    }.toULongArray()
