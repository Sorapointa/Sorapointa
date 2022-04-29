package org.sorapointa

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.sorapointa.plugins.configureHTTP
import org.sorapointa.plugins.configureMonitoring
import org.sorapointa.plugins.configureRouting
import org.sorapointa.plugins.configureSerialization

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSerialization()
        configureMonitoring()
        configureHTTP()
    }.start(wait = true)
}
