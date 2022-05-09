package org.sorapointa.utils.encoding

import java.util.*
import java.util.Base64.Decoder
import java.util.Base64.Encoder

/**
 * Base64 interface
 */
interface Base64Provider {
    fun encode(data: ByteArray): String

    fun decode(data: String): ByteArray

    fun encode(data: String): String

    fun decodeToString(data: String): String
}

fun String.decodeBase64(provider: Base64Provider = Base64Impl.Default): ByteArray =
    provider.decode(this)

fun String.decodeBase64ToString(provider: Base64Provider = Base64Impl.Default): String =
    provider.decodeToString(this)

fun ByteArray.encodeBase64(provider: Base64Provider = Base64Impl.Default): String =
    provider.encode(this)

fun String.encodeBase64(provider: Base64Provider = Base64Impl.Default): String =
    provider.encode(this)

/**
 * Base64 implementation based on JVM Base64
 *
 * **All available instance are in companion object.**
 *
 * @see Base64Provider
 * @see [Base64]
 */
class Base64Impl private constructor(
    private val encoder: Encoder,
    private val decoder: Decoder,
) : Base64Provider {
    companion object {
        /**
         * Default base64 instance
         */
        val Default: Base64Provider = Base64Impl(Base64.getEncoder(), Base64.getDecoder())

        /**
         * Url and file safe base64 instance
         */
        val UrlSafe: Base64Provider = Base64Impl(Base64.getUrlEncoder(), Base64.getUrlDecoder())

        private val BASE64_CHARSET = Charsets.UTF_8
    }

    override fun encode(data: ByteArray): String = String(encoder.encode(data), BASE64_CHARSET)

    override fun encode(data: String): String = encode(data.toByteArray(BASE64_CHARSET))

    override fun decode(data: String): ByteArray = decoder.decode(data.toByteArray(BASE64_CHARSET))

    override fun decodeToString(data: String): String = String(decode(data), BASE64_CHARSET)
}
