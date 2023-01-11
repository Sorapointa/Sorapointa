@file:Suppress("unused")

package org.sorapointa.utils.encoding

import org.sorapointa.utils.toInt
import java.security.MessageDigest

fun String.md5() =
    hashString(this, "MD5")

fun ByteArray.md5() =
    hash(this, "MD5")

fun String.sha256() =
    hashString(this, "SHA-256")

fun ByteArray.sha256() =
    hash(this, "SHA-256")

private fun hashString(input: String, algorithm: String) =
    hash(input, algorithm).hex

private fun hash(input: String, algorithm: String): ByteArray =
    hash(input.toByteArray(), algorithm)

private fun hash(input: ByteArray, algorithm: String): ByteArray =
    MessageDigest
        .getInstance(algorithm)
        .digest(input)

fun bkdrHash(str: String): Int =
    str.fold(0) { acc, char -> char.code + 131 * acc }

infix fun Int.combineHash(suffix: Long) =
    ((this.toLong()) shl 32) or suffix

/*
md5 -> 7be8d6309953f335ed9c7fb91741cf82
prefix: 7b, suffix: e8d63099 -> LE -> 9930d6e8
 */
private fun ByteArray.segmentHash() =
    SegmentHash(this[0].toUShort(), (this.slice(1..4).toByteArray()).toInt().toUInt())

fun getPrefixSuffixHash(str: String): SegmentHash {
    val bytes = str.toByteArray(Charsets.US_ASCII)
    val size = ((str.length shr 8) + 1) shl 8
    val newBytes = bytes.copyInto(ByteArray(size))
    return newBytes.md5().segmentHash()
}

data class SegmentHash(
    val prefixHash: UShort,
    val suffixHash: UInt,
) {

    val combinedHash by lazy {
        prefixHash.toInt() combineHash suffixHash.toLong()
    }
}
