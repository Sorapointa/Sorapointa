package org.sorapointa.utils.crypto

import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sorapointa.utils.*
import org.w3c.dom.Document
import java.math.BigInteger
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

/**
 * RSA key from C# format
 *
 * @see parseRSAKey
 * You could **NOT** use the [RSAKey.decrypt] method
 * without the big number `d` parameter.
 */
class RSAKey(
    modulus: BigInteger,
    exponent: BigInteger,
    d: BigInteger?,
) {
    private val keySize = modulus.bitLength()
    private val factory = KeyFactory.getInstance("RSA")
    private val cipher = Cipher.getInstance("RSA")
    private val signature: Signature = Signature.getInstance("SHA256withRSA")
    private val privateSpec by lazy {
        d?.let { RSAPrivateKeySpec(modulus, d) }
    }
    private val privateKey by lazy {
        privateSpec?.let { factory.generatePrivate(privateSpec) }
    }
    private val publicSpec = RSAPublicKeySpec(modulus, exponent)
    private val publicKey = factory.generatePublic(publicSpec)

    suspend fun ByteArray.encrypt(): ByteArray = withContext(Dispatchers.Default) {
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        chunkCipher(keySize / 8 - 11)
    }

    suspend fun ByteArray.decrypt(): ByteArray = withContext(Dispatchers.Default) {
        privateKey?.let {
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            chunkCipher(keySize / 8)
        } ?: error("Component of private key, `d`, has not been given in constructor")
    }

    suspend fun ByteArray.signVerify(sign: ByteArray): Boolean = withContext(Dispatchers.Default) {
        signature.initVerify(publicKey)
        signature.update(this@signVerify)
        signature.verify(sign)
    }

    suspend fun ByteArray.sign(): ByteArray = withContext(Dispatchers.Default) {
        privateKey?.let {
            signature.initSign(it)
            signature.update(this@sign)
            signature.sign()
        } ?: error("Component of private key, `d`, has not been given in constructor")
    }

    private fun ByteArray.chunkCipher(chunkSize: Int): ByteArray {
        val packet = this.toReadPacket()
        return buildPacket {
            while (packet.canRead()) {
                val readSize = if (packet.remaining > chunkSize) chunkSize else packet.remaining.toInt()
                writeFully(cipher.doFinal(packet.readBytes(readSize)))
            }
        }.readBytes()
    }
}

/**
 * Quick way to parse a xml RSA key to [RSAKey]
 */
fun String.parseToRSAKey(): RSAKey? =
    parseRSAKey(readToXMLDocument())

/**
 * Convert C# XML RSA key to [RSAKey]
 *
 * @param dom XML [Document]
 * @see readToXMLDocument
 * @return nullable [RSAKey], if document doesn't contain
 * the RSA key modulus or exponent, it will return null.
 * If document doesn't contain the RSA key big number `d`,
 * you could **NOT** use the [RSAKey.decrypt] method.
 */
fun parseRSAKey(dom: Document): RSAKey? {
    val root = dom.byTagFirst("RSAKeyValue")?.childNodes?.toList() ?: return null
    val modulus = root.getTextByName("Modulus") ?: return null
    val exponent = root.getTextByName("Exponent") ?: return null
    val d = root.getTextByName("D")
    return RSAKey(
        modulus.decodeBase64Bytes().toBigInteger(),
        exponent.decodeBase64Bytes().toBigInteger(),
        d?.decodeBase64Bytes()?.toBigInteger()
    )
}

private fun ByteArray.toBigInteger() =
    BigInteger(1, this)
