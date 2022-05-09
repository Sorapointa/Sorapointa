package org.sorapointa

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.sorapointa.DispatchServer.setupApplication
import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.plugins.configureHTTP
import org.sorapointa.plugins.configureMonitoring
import org.sorapointa.plugins.configureRouting
import org.sorapointa.plugins.configureSerialization
import org.sorapointa.utils.absPath
import org.sorapointa.utils.configDirectory
import java.io.File

// private val logger = KotlinLogging.logger {}

fun main(): Unit = runBlocking {
    DispatchConfig.init()
    val environment = DispatchServer.getEnvironment()
    environment.setupApplication()
    launch(Dispatchers.IO) {
        embeddedServer(Netty, environment = environment).start(wait = true)
    }.join()
}

object DispatchServer {

    fun getEnvironment() = applicationEngineEnvironment {
        val dispatchConfig = DispatchConfig.data
        log = LoggerFactory.getLogger("ktor.application")
        if (dispatchConfig.useSSL) {
            sslConnector(
                keyStore = KeyProvider.getCerts(),
                keyAlias = dispatchConfig.keyAlias,
                keyStorePassword = { dispatchConfig.keyStorePassword.toCharArray() },
                privateKeyPassword = { dispatchConfig.privateKeyPassword.toCharArray() }
            ) {
                host = dispatchConfig.host
                port = dispatchConfig.port
                keyStorePath = File(dispatchConfig.keyStoreFilePath)
            }
        } else {
            connector {
                host = dispatchConfig.host
                port = dispatchConfig.port
            }
        }
    }

    fun ApplicationEngineEnvironment.setupApplication() {
        this.application.apply {
            configureRouting()
            configureSerialization()
            configureMonitoring()
            configureHTTP()
        }
    }

    fun startDispatch(port: Int, host: String) {
        embeddedServer(Netty, port = port, host = host) {
            configureRouting()
            configureSerialization()
            configureMonitoring()
            configureHTTP()
        }.start()
    }
}

object DispatchConfig : DataFilePersist<DispatchConfig.Data>(
    File(configDirectory, "dispatchConfig.json"), Data()
) {

    @kotlinx.serialization.Serializable
    data class Data(
        var host: String = "0.0.0.0",
        var port: Int = 443,
        var useSSL: Boolean = true,
        var keyStoreFilePath: String = File(configDirectory, KeyProvider.DEFAULT_CERT_NAME).absPath,
        var keyStore: String = "JKS",
        var keyAlias: String = KeyProvider.DEFAULT_ALIAS,
        var keyStorePassword: String = KeyProvider.DEFAULT_KEY_STORE_PASSWORD,
        var privateKeyPassword: String = KeyProvider.DEFAULT_CERT_PASSWORD
    )
}
