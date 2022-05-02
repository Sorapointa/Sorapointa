package org.sorapointa

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.sorapointa.DispatchServer.setupApplication
import org.sorapointa.plugins.configureHTTP
import org.sorapointa.plugins.configureMonitoring
import org.sorapointa.plugins.configureRouting
import org.sorapointa.plugins.configureSerialization
import java.io.File
import java.security.KeyStore

//private val logger = KotlinLogging.logger {}

fun main(): Unit = runBlocking {
    val environment =  DispatchServer.getEnvironment(8080, "0.0.0.0", KeyProvider.getKey())
    environment.setupApplication()
    launch(Dispatchers.IO) {
        embeddedServer(Netty, environment = environment).start(wait = true)
    }.join()
}

object DispatchServer {

    fun getEnvironment(dispatchPort: Int,
                       dispatchHost: String,
                       keyData: KeyProvider.KeyData?
    ) = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("ktor.application")
        connector {
            port = dispatchPort
            host = dispatchHost
        }
        keyData?.also {
            sslConnector(
                keyStore = KeyStore.getInstance(keyData.keyStore),
                keyAlias = keyData.keyAlias,
                keyStorePassword = { keyData.keyStorePassword.toCharArray() },
                privateKeyPassword = { keyData.privateKeyPassword.toCharArray() }) {
                port = 8443
                keyStorePath = File(keyData.keyStoreFilePath)
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

