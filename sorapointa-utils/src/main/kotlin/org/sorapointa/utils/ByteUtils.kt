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

fun xor(data: ByteArray, key: ByteArray) {
    for (i in data.indices) {
        data[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
    }
}
