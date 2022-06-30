package org.sorapointa.dispatch.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.*

internal fun Application.configureMonitoring() {
    install(CallLogging) {
        format { call ->
            when (val status = call.response.status() ?: "Unhandled") {
                HttpStatusCode.Found ->
                    "${status as HttpStatusCode}: " +
                        "${call.request} -> ${call.response.headers[HttpHeaders.Location]}"
                "Unhandled" -> "$status: ${call.request}"
                else -> "${status as HttpStatusCode}: ${call.request}"
            }
        }
        level = Level.DEBUG
        filter { call -> call.request.path().startsWith("/") }
    }
}
