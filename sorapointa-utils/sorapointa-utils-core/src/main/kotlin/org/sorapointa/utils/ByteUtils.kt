@file:Suppress("unused")

package org.sorapointa.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.xor

/**
 * Convert [Int] to [ByteArray]
 *
 * @param littleEndian convert to LE or BE
 */
fun Int.toByteArray(littleEndian: Boolean = true) = ByteArray(4).also { a ->
    a[0] = shr(if (littleEndian) 0 else 24).toByte()
    a[1] = shr(if (littleEndian) 8 else 16).toByte()
    a[2] = shr(if (littleEndian) 16 else 8).toByte()
    a[3] = shr(if (littleEndian) 24 else 0).toByte()
}

/**
 * Convert [UInt] to [ByteArray]
 *
 * @param littleEndian convert to LE or BE
 */
fun UInt.toByteArray(littleEndian: Boolean = true) = toInt().toByteArray(littleEndian)

/**
 * Convert [ByteArray] to [Int]
 *
 * @param byteOrder convert to LE or BE
 */
fun ByteArray.toInt(byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN) =
    ByteBuffer.wrap(this).order(byteOrder).int

/**
 * Convert [ByteArray] to [UInt]
 *
 * @param byteOrder convert to LE or BE
 */
fun ByteArray.toUInt(byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN) =
    toInt(byteOrder).toUInt()

/**
 * Convert [Long] to [ByteArray]
 *
 * @param littleEndian convert to LE or BE
 */
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

/**
 * Convert [ULong] to [ByteArray]
 *
 * @param littleEndian convert to LE or BE
 */
fun ULong.toByteArray(littleEndian: Boolean = true) = toLong().toByteArray(littleEndian)

/**
 * Convert [ByteArray] to [Int]
 *
 * @param byteOrder convert to LE or BE
 */
fun ByteArray.toLong(byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN) =
    ByteBuffer.wrap(this).order(byteOrder).long

/**
 * Convert [ByteArray] to [UInt]
 *
 * @param byteOrder convert to LE or BE
 */
fun ByteArray.toULong(byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN) =
    toLong(byteOrder).toULong()

/**
 * [UByteArray] xor [UByteArray]
 *
 * @param key xor key
 */
infix fun UByteArray.xor(key: UByteArray): UByteArray {
    for (i in this.indices) {
        this[i] = this[i] xor key[i % key.size]
    }
    return this
}

/**
 * [ByteArray] xor [ByteArray]
 *
 * @param key xor key
 */
infix fun ByteArray.xor(key: ByteArray): ByteArray {
    for (i in this.indices) {
        this[i] = this[i] xor key[i % key.size]
    }
    return this
}
