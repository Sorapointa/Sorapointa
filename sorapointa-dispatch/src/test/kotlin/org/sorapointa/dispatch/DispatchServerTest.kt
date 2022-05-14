package org.sorapointa.dispatch

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.sorapointa.dispatch.plugins.configureRouting
import kotlin.test.Test
import kotlin.test.assertEquals

class DispatchServerTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello Sorapointa! The Dispatch Server has been successfully launched!", bodyAsText())
        }
    }
}
