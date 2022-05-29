@file:Suppress("unused")

package org.sorapointa.utils.crypto

import org.sorapointa.utils.encoding.hex
import java.security.MessageDigest

fun String.md5Str(): String {
    return hashString(this, "MD5")
}

fun String.sha256Str(): String {
    return hashString(this, "SHA-256")
}

fun String.md5(): String {
    return hashString(this, "MD5")
}

fun String.sha256(): String {
    return hashString(this, "SHA-256")
}

private fun hashString(input: String, algorithm: String): String {
    return hash(input, algorithm).hex
}

private fun hash(input: String, algorithm: String): ByteArray =
    MessageDigest
        .getInstance(algorithm)
        .digest(input.toByteArray())
