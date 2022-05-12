package org.sorapointa.dispatch

import io.ktor.network.tls.extensions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.sorapointa.dispatch.util.KeyType
import org.sorapointa.dispatch.util.buildKeyStore
import org.sorapointa.dispatch.util.saveCertToFile
import org.sorapointa.dispatch.util.saveToFile
import org.sorapointa.utils.isJUnitTest
import java.io.File
import java.security.KeyStore
import java.util.*

private val logger = KotlinLogging.logger {}

object KeyProvider {

    const val DEFAULT_CERT_NAME = "sorapointa-cert.jks"
    const val DEFAULT_ALIAS = "sorapointa-dispatch-cert"
    const val DEFAULT_CERT_PASSWORD = "sorapointa-dispatch-private"
    const val DEFAULT_KEY_STORE_PASSWORD = "sorapointa-dispatch"
    const val DEFAULT_EXPIRED_DAYS = 365 * 10L // 10 Years

    fun getCerts(): KeyStore = runBlocking {
        val dispatchConfig = DispatchConfig.data
        val keyStoreFile = File(dispatchConfig.certificationConfig.keyStoreFilePath)
        if (!keyStoreFile.exists()) {
            logger.info { "Input alias for certification or set default $DEFAULT_ALIAS" }
            val alias: String = DEFAULT_ALIAS.waitInputOrDefault()
            logger.info { "Input password for certification or set default $DEFAULT_CERT_PASSWORD" }
            val privateKeyPassword: String = DEFAULT_CERT_PASSWORD.waitInputOrDefault()
            logger.info { "Input password for key store or set default $DEFAULT_KEY_STORE_PASSWORD" }
            val keyStorePassword: String = DEFAULT_KEY_STORE_PASSWORD.waitInputOrDefault()
            logger.info { "Input valid days for certification or set default $DEFAULT_EXPIRED_DAYS" }
            val expiredDays: Long = DEFAULT_EXPIRED_DAYS.waitInputOrDefault().toLongOrNull() ?: DEFAULT_EXPIRED_DAYS
            val generatedKeyStore = buildKeyStore {
                certificate(alias) {
                    hash = HashAlgorithm.SHA256
                    sign = SignatureAlgorithm.RSA
                    keySizeInBits = 2048
                    password = privateKeyPassword
                    daysValid = expiredDays
                    hosts = listOf("localhost", "*.mihoyo.com", "*.yuanshen.com", "*.hoyoverse.com")
                    keyType = KeyType.Server
                }
            }
            generatedKeyStore.saveToFile(keyStoreFile, keyStorePassword)
            generatedKeyStore.saveCertToFile(
                File(keyStoreFile.parentFile, keyStoreFile.nameWithoutExtension + ".cert"), alias
            )
            dispatchConfig.certificationConfig = DispatchConfig.Certification(
                keyStore = "JKS",
                keyAlias = alias,
                keyStorePassword = keyStorePassword,
                privateKeyPassword = privateKeyPassword
            )
            DispatchConfig.save()
            return@runBlocking generatedKeyStore
        } else {
            withContext(Dispatchers.IO) {
                fromCertFile(keyStoreFile, dispatchConfig.certificationConfig.keyStorePassword)
            }
        }
    }

    private fun fromCertFile(certFile: File, password: String): KeyStore {
        val store = KeyStore.getInstance("JKS")!!
        store.load(certFile.inputStream(), password.toCharArray())
        return store
    }

    private fun <T> T.waitInputOrDefault(): String {
        return if (!isJUnitTest()) Scanner(System.`in`).nextLine()?.takeIf { it != "" }
            ?: this.toString() else this.toString()
    }
}
