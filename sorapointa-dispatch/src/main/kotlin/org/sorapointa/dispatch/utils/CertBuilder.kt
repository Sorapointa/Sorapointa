package org.sorapointa.dispatch.utils

import io.ktor.network.tls.*
import io.ktor.network.tls.extensions.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import java.io.File
import java.net.Inet4Address
import java.net.InetAddress
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.text.toCharArray

internal data class CertificateInfo(val certificate: Certificate, val keys: KeyPair, val password: String)

/**
 * Builder for certificate
 */
@Suppress("MemberVisibilityCanBePrivate")
internal class CertificateBuilder {
    /**
     * Certificate hash algorithm (required)
     */
    lateinit var hash: HashAlgorithm

    /**
     * Certificate signature algorithm (required)
     */
    lateinit var sign: SignatureAlgorithm

    /**
     * Certificate password (required)
     */
    lateinit var password: String

    /**
     * Number of days the certificate is valid
     */
    var daysValid: Long = 3

    /**
     * Certificate key size in bits
     */
    var keySizeInBits: Int = 1024

    private val counterparty = Counterparty(
        country = "US",
        organization = "Sorapointa",
        organizationUnit = "GI",
        commonName = "Sorapointa",
    )

    var hosts: List<String> = listOf("localhost")

    var ipAddress: List<InetAddress> = listOf(Inet4Address.getByName("127.0.0.1"))

    var keyType: KeyType = KeyType.Server

    internal fun build(): CertificateInfo {
        val algorithm = HashAndSign(hash, sign)
        val keys = KeyPairGenerator.getInstance(keysGenerationAlgorithm(algorithm.name))!!.apply {
            initialize(keySizeInBits)
        }.genKeyPair()!!

        val id = counterparty
        val from = Date()
        val to = Date.from(LocalDateTime.now().plusDays(daysValid).atZone(ZoneId.systemDefault()).toInstant())

        val certificateBytes = buildPacket {
            writeCertificate(
                issuer = id,
                subject = id,
                keyPair = keys,
                algorithm = algorithm.name,
                from = from,
                to = to,
                domains = hosts,
                ipAddresses = ipAddress,
                keyType = keyType,
            )
        }.readBytes()

        val cert = CertificateFactory.getInstance("X.509").generateCertificate(certificateBytes.inputStream())
        cert.verify(keys.public)
        return CertificateInfo(cert, keys, password)
    }
}

/**
 * Builder for key store
 */
internal class KeyStoreBuilder internal constructor() {

    var keyStore: String = "JKS"

    private val certificates = mutableMapOf<String, CertificateInfo>()

    /**
     * Generate a certificate and append to the key store.
     * If there is a certificate with the same [alias] then it will be replaced
     */
    internal fun certificate(alias: String, block: CertificateBuilder.() -> Unit) {
        certificates[alias] = CertificateBuilder().apply(block).build()
    }

    internal fun build(): KeyStore {
        val store = KeyStore.getInstance(keyStore)
            ?: error("Key store $keyStore is not supported")
        store.load(null, null)

        certificates.forEach { (alias, info) ->
            val (certificate, keys, password) = info
            store.setCertificateEntry(alias, certificate)
            store.setKeyEntry(alias, keys.private, password.toCharArray(), arrayOf(certificate))
        }

        return store
    }
}

/**
 * Create a keystore and configure it in [block] function
 */
internal fun buildKeyStore(block: KeyStoreBuilder.() -> Unit): KeyStore = KeyStoreBuilder().apply(block).build()

/**
 * Save [KeyStore] to [output] file with the specified [password]
 */
internal fun KeyStore.saveToFile(output: File, password: String) {
    output.parentFile?.mkdirs()

    output.outputStream().use {
        store(it, password.toCharArray())
    }
}

internal fun KeyStore.saveCertToFile(output: File, alias: String) {
    output.parentFile?.mkdirs()
    val certBegin = "-----BEGIN CERTIFICATE-----\n"
    val endCert = "-----END CERTIFICATE-----"
    output.outputStream().use {
        it.write(certBegin.toByteArray())
        it.write(getCertificate(alias).encoded.encodeBase64().toByteArray())
        it.write(endCert.toByteArray())
    }
}
