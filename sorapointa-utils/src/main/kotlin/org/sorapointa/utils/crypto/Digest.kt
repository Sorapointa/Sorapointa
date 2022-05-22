@file:Suppress("unused")

package org.sorapointa.utils.crypto

import mu.KotlinLogging
import org.sorapointa.utils.encoding.hex
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val logger = KotlinLogging.logger {}

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

val secureRandom: Random
    get() = SecureRandom.getInstanceStrong()
        ?: SecureRandom.getInstance("NativePRNG")
        ?: SecureRandom.getInstance("SHA1PRNG")
        ?: run {
            logger.warn {
                "No SecureRandom Instance found, fallback to normal Random, " +
                    "please change your JDK vendor for better safety."
            }
            ThreadLocalRandom.current()
        }

// TODO: Move to proper package
fun randomByteArray(length: Int): ByteArray {
    val bytes = ByteArray(length)
    secureRandom.nextBytes(bytes)
    return bytes
}

fun randomUByteArray(length: UInt): UByteArray =
    randomByteArray(length.toInt()).toUByteArray()

fun sha256sign(data: String, key: String): String {
    val sha256Hmac = Mac.getInstance("HmacSHA256")
    val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
    sha256Hmac.init(secretKey)
    return sha256Hmac.doFinal(data.toByteArray()).hex
}
