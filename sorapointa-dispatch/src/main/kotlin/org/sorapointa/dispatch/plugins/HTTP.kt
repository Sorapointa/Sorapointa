package org.sorapointa.dispatch.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*

internal fun Application.configureHTTP() {
    install(Compression) {
        gzip()
        deflate()
    }
}
