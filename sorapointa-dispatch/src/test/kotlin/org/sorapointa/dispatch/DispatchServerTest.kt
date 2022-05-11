package org.sorapointa.dispatch

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
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

    @Test
    fun `test mihoyo`() = runBlocking {
        val cioClient = HttpClient(CIO) {
            install(Logging)
        }

        val response = cioClient.get("https://sdk-static.mihoyo.com/hk4e_cn/mdk/shield/api/loadConfig?client=3&game_key=hk4e_cn").bodyAsText()
        println(response)
    }

}
