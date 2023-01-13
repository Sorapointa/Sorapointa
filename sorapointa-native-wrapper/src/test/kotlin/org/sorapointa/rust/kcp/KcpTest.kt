package org.sorapointa.rust.kcp

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import org.sorapointa.rust.logging.RustLogger
import java.io.IOException
import java.net.InetSocketAddress
import kotlin.random.Random
import kotlin.test.assertFailsWith

class MockKcpHandler(
    kcpListener: KcpListener = KcpListener(
        config = KcpConfig(),
        addressString = "127.0.0.1:${Random.nextInt(10000, 20000) % UShort.MAX_VALUE.toInt()}",
    ),
) : KcpHandler(kcpListener) {
    init {
        init()
    }

    override fun onRecv(socketAddress: InetSocketAddress, bytes: ByteArray) {
        send(socketAddress, bytes)
    }

    override fun onPeerAccept(socketAddress: InetSocketAddress) {
    }

    override fun onPeerClose(socketAddress: InetSocketAddress) {
    }
}

class KcpTest {
    @Test
    fun `cannot be closed twice`() {
        val a = MockKcpHandler()
        println(a)
        a.close()
        assertFailsWith<IOException> {
            a.close()
        }
    }

    @Test
    fun `cannot do anything after closing`() {
        val a = MockKcpHandler()
        println(a)
        a.close()
        assertFailsWith<IllegalStateException> {
            a.start().abort()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun coroutineTest(): Unit = runBlocking {
        RustLogger.setup()
        val mockKcpHandler = MockKcpHandler()
        val handle = mockKcpHandler.start()
        val kcpThread = newSingleThreadContext("KCP")
        launch(kcpThread) {
            handle.await()
        }
        delay(500)
        handle.abort()
    }
}
