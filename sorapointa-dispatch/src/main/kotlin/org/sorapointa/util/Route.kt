package org.sorapointa.util

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

/**
 * Route for both get and post
 * @see [get]
 * @see [body]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Route.getAndPost(path: String, noinline body: PipelineInterceptor<Unit, ApplicationCall>) {
    get(path, body)
    post(path, body)
}
