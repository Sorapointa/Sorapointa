package org.sorapointa.utils.crypto

import io.ktor.util.*
import io.ktor.utils.io.core.*
import org.sorapointa.utils.*
import org.w3c.dom.Document
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPrivateKeySpec
import javax.crypto.Cipher

class RSAKey(
    modulus: BigInteger,
    d: BigInteger,
) {
    private val factory = KeyFactory.getInstance("RSA")
    private val cipher = Cipher.getInstance("RSA")
    private val privateSpec = RSAPrivateKeySpec(modulus, d)
    private val privateKey = factory.generatePrivate(privateSpec)

    init {
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
    }

    fun ByteArray.decrypt(): ByteArray {
        val packet = this.toReadPacket()
        val keySize = privateSpec.modulus.bitLength()
        val chunkSize = keySize / 8
        return buildPacket {
            while (packet.canRead()) {
                writeFully(cipher.doFinal(packet.readBytes(chunkSize)))
            }
        }.readBytes()
    }
}

fun String.parseToRSAKey(): RSAKey? =
    parseRSAKey(readToXMLDocument())

fun parseRSAKey(dom: Document): RSAKey? {
    val root = dom.byTagFirst("RSAKeyValue")?.childNodes?.toList() ?: return null
    val modulus = root.getTextByName("Modulus") ?: return null
    val d = root.getTextByName("D") ?: return null
    return RSAKey(
        modulus.decodeBase64Bytes().toBigInteger(),
        d.decodeBase64Bytes().toBigInteger()
    )
}

private fun ByteArray.toBigInteger() =
    BigInteger(1, this)
