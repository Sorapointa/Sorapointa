package org.sorapointa.dispatch.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*


internal fun Application.configureStatusPage() {
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondText(text = "404: Page Not Found | Powered by Sorapointa", status = status)
        }
        status(HttpStatusCode.InternalServerError) { call, status ->
            call.respondText(text = "500: Internal Server Error | Powered by Sorapointa", status = status)
        }
    }
}
