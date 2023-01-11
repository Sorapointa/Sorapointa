package org.sorapointa.utils

import mu.KotlinLogging
import org.sorapointa.utils.encoding.hex
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val logger = KotlinLogging.logger {}

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

fun randomByteArray(length: Int): ByteArray {
    val bytes = ByteArray(length)
    secureRandom.nextBytes(bytes)
    return bytes
}

/**
 * Random unsigned int in [0, Int_MAX)
 */
fun randomUInt(): UInt =
    kotlin.random.Random.nextInt(0, Int.MAX_VALUE).toUInt()

/**
 * Random unsigned long in [0, Long_MAX)
 */
fun randomULong(): ULong =
    kotlin.random.Random.nextLong(0, Long.MAX_VALUE).toULong()

/**
 * Random unsigned byte array
 *
 * @param length byte length
 */
fun randomUByteArray(length: UInt): UByteArray =
    randomByteArray(length.toInt()).toUByteArray()

/**
 * SHA 256 Signature
 *
 * @receiver data to be signed
 * @param key sign key
 * @return sign result
 */
fun String.sha256sign(key: String): String {
    val sha256Hmac = Mac.getInstance("HmacSHA256")
    val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
    sha256Hmac.init(secretKey)
    return sha256Hmac.doFinal(this.toByteArray()).hex
}

fun Collection<Int>.partialSum(): List<Int> {
    var sum = 0
    return map { i ->
        (i + sum).also { sum = it }
    }
}

fun Collection<Int>.weightRandom(): Int {
    val ps = partialSum()
    val random = (1..ps.last()).random()
    var l = 0
    var r = ps.size - 1
    while (l < r) {
        val mid = (l + r) ushr 1 // safe from overflows
        if (ps[mid] < random) {
            l = mid + 1
        } else {
            r = mid
        }
    }
    return l
}

fun <K> Map<K, Int>.weightRandom(): K =
    keys.elementAt(values.weightRandom())
