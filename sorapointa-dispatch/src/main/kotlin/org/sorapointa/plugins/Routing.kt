package org.sorapointa.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello Sorapointa! The Dispatch Server has been successfully launched!")
        }
        get("/query_region_list") {
            logger.info { "Client ${call.request.local.host} has queried region list" }
            
        }

    }
}
