package org.sorapointa.rust.kcp

import mu.KotlinLogging
import org.sorapointa.rust.initRustLibrary
import org.sorapointa.rust.tokio.TokioHandle
import java.io.Closeable
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger { }

fun KcpListener(config: KcpConfig, addressString: String): KcpListener {
    val nativePtr = KcpListener.newNative(config, addressString)
    return KcpListener(nativePtr)
}

class KcpListener internal constructor(
    private val nativePointer: Long,
) : Closeable, AutoCloseable {
    internal lateinit var handler: KcpHandler

    private val closed: AtomicBoolean = AtomicBoolean(false)
    private val map = ConcurrentHashMap<InetSocketAddress, Long>()

    private external fun mainLoop0(): Long

    fun mainLoop(): TokioHandle = TokioHandle(mainLoop0())

    // return 0 if err
    private external fun send0(ptr: Long, bytes: ByteArray): Long

    /**
     * **WARNING**: Return value `TokioHandle` must be closed,
     * you can call [use] to auto close
     */
    fun send(socketAddress: InetSocketAddress, bytes: ByteArray): TokioHandle {
        val ptr = map[socketAddress] ?: error("Address is not in sessions: $socketAddress")
        val handlePtr = send0(ptr, bytes)
        return TokioHandle(handlePtr)
    }

    /**
     * Send and blocking until native async closure finished
     * @see send
     */
    fun sendBlocking(socketAddress: InetSocketAddress, bytes: ByteArray) {
        val handle = send(socketAddress, bytes)
        handle.use {
            while (handle.isFinished()) {
                // wait
            }
        }
    }

    @Suppress("unused") // callback for native
    private fun onRecv0(ip: ByteArray, port: Int, bytes: ByteArray) {
        val ipAddr = InetAddress.getByAddress(ip)
        val addr = InetSocketAddress(ipAddr, port)
        handler.onRecv(addr, bytes)
    }

    @Suppress("unused") // callback for native
    private fun onPeerAccept0(ptr: Long, ip: ByteArray, port: Int) {
        val ipAddr = InetAddress.getByAddress(ip)
        val addr = InetSocketAddress(ipAddr, port)
        map[addr] = ptr
        logger.debug { "peer accep, addr = $addr, session ptr = 0x${ptr.toString(16)}" }
        handler.onPeerAccept(addr)
    }

    @Suppress("unused") // callback for native
    private fun onPeerClose0(ptr: Long, ip: ByteArray, port: Int) {
        val ipAddr = InetAddress.getByAddress(ip)
        val addr = InetSocketAddress(ipAddr, port)
        map.remove(addr)
        logger.debug { "peer close, addr = $addr, session ptr = 0x${ptr.toString(16)}" }
        handler.onPeerClose(addr)
    }

    override fun toString(): String {
        return "KcpListener(" +
            "nativePointer=0x${nativePointer.toString(16)}, " +
            "closed=${closed.get()}" +
            ")"
    }

    external override fun close()

    companion object {
        init {
            initRustLibrary()
        }

        @JvmStatic
        @JvmName("newNative")
        internal external fun newNative(config: KcpConfig, addressString: String): Long
    }
}

abstract class KcpHandler(
    private val kcp: KcpListener,
) : Closeable, AutoCloseable {
    /**
     * Must be called by subclass in `init` block
     */
    fun init() {
        kcp.handler = this
    }

    fun start() = kcp.mainLoop()

    /**
     * Callback when receive a KCP packet
     */
    abstract fun onRecv(socketAddress: InetSocketAddress, bytes: ByteArray)

    abstract fun onPeerAccept(socketAddress: InetSocketAddress)

    abstract fun onPeerClose(socketAddress: InetSocketAddress)

    /**
     * @see [KcpListener.send]
     */
    fun send(socketAddress: InetSocketAddress, bytes: ByteArray): TokioHandle = kcp.send(socketAddress, bytes)

    /**
     * @see [KcpListener.sendBlocking]
     */
    fun sendBlocking(socketAddress: InetSocketAddress, bytes: ByteArray): Unit = kcp.sendBlocking(socketAddress, bytes)

    override fun close() {
        kcp.close()
    }

    override fun toString(): String {
        return "KcpHandler(kcp=$kcp)"
    }
}
