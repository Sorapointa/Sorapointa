package org.sorapointa

import io.ktor.network.tls.certificates.*
import io.ktor.network.tls.extensions.*
import mu.KotlinLogging
import java.io.File
import java.util.*

private val logger = KotlinLogging.logger {}

object KeyProvider {

    private const val DEFAULT_CERT_NAME = "sorapointa-cert.jks"
    private const val DEFAULT_ALIAS = "sorapointa-dispatch-cert"
    private const val DEFAULT_CERT_PASSWORD = "sorapointa-dispatch"
    private const val DEFAULT_EXPIRED_DAYS = 365 * 10L // 10 Years

    private val defaultCertPath by lazy {
        "${System.getProperty("user.dir")}/$DEFAULT_CERT_NAME"
    }

    @kotlinx.serialization.Serializable
    data class KeyData(
        var keyStoreFilePath: String = defaultCertPath,
        var keyStore: String = "JSK",
        var keyAlias: String = DEFAULT_ALIAS,
        var keyStorePassword: String = DEFAULT_CERT_PASSWORD,
        var privateKeyPassword: String = DEFAULT_CERT_PASSWORD
    )

    private fun buildKeyData(builder: KeyData.() -> Unit): KeyData {
        return KeyData().apply(builder)
    }

    fun getKey(keyPath: String = defaultCertPath): KeyData {
        val keyStoreFile = File(keyPath)
        return buildKeyData {
            keyStoreFilePath = keyPath
            if (!keyStoreFile.exists()) {
                logger.info { "Input alias for certification or set default $DEFAULT_ALIAS" }
                val alias: String = DEFAULT_ALIAS.waitInputOrDefault()
                logger.info { "Input passwrod for certification or set default $DEFAULT_CERT_PASSWORD" }
                val certPassword: String = DEFAULT_CERT_PASSWORD.waitInputOrDefault()
                logger.info { "Input valid days for certification or set default $DEFAULT_EXPIRED_DAYS" }
                val expiredDays: Long = DEFAULT_EXPIRED_DAYS.waitInputOrDefault().toLongOrNull() ?: DEFAULT_EXPIRED_DAYS
                val generatedKeyStore = buildKeyStore {
                    certificate(alias) {
                        hash = HashAlgorithm.SHA256
                        sign = SignatureAlgorithm.RSA
                        keySizeInBits = 2048
                        password = certPassword
                        daysValid = expiredDays
                    }
                }
                generatedKeyStore.saveToFile(keyStoreFile, certPassword)
                keyAlias = alias
                keyStorePassword = certPassword
                privateKeyPassword = certPassword
            } else {
                // TODO: ... 读取数据
            }
        }
    }

    private fun <T> T.waitInputOrDefault(): String {
        return Scanner(System.`in`).nextLine()?.takeIf { it != "" } ?: this.toString()
    }
}

class Test {

    operator fun String.times(str: Any) {
    }
}

/*

Java -> pass value as function


fun buildChain(builder: MessageChainBuilder.() -> MessageChain )

sender.sendMessage {
    buildI18nMsg(
        key = "command.test",
        chain = buildChain { "Command" * T * " exec succ" }
    )
}

sender.sendMessage { "command.test".localize(T) }

XXXStartUp Method
fun xxxxInit(): XXX {
    ...

    registerI18nMsg("command.test", "Command {0} exec succ")

    ...
}

val COMMAND_TEST = buildChain { "Command" * T * " exec succ" }

sender.sendMessage { buildI18nMsg("command.test", buildChain { "Command " * T * " exec succ"}) }

buildI18nMsg 会自动从 i18n 配置文件优先读取 key，然后替换 placeholder，如果配置没有，就使用默认的 str 字符串并自动生成语言配置。

buildChain 内的 Scope 会重载 String 乘法操作运算符号，使得 buildChain 可以自动生成对应字符串的 placeHolder


registerCommand("xxx") {

}


 */
