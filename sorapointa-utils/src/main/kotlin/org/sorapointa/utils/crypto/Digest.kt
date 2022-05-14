package org.sorapointa.utils.crypto

import org.sorapointa.utils.encoding.hex
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

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

fun randomByteArray(length: Int): ByteArray {
    val bytes = ByteArray(length)
    SecureRandom().nextBytes(bytes)
    return bytes
}

fun sha256sign(data: String, key: String): String {
    val sha256Hmac = Mac.getInstance("HmacSHA256")
    val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
    sha256Hmac.init(secretKey)
    return sha256Hmac.doFinal(data.toByteArray()).hex
}
