package org.sorapointa.server.network

import io.jpower.kcp.netty.UkcpChannel
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.MessageToMessageDecoder
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.sorapointa.dispatch.data.DispatchKeyData
import org.sorapointa.event.StateController
import org.sorapointa.event.WithState
import org.sorapointa.event.broadcast
import org.sorapointa.event.broadcastEvent
import org.sorapointa.events.AfterSendIncomingPacketResponseEvent
import org.sorapointa.events.HandleRawSoraPacketEvent
import org.sorapointa.events.SendOutgoingPacketEvent
import org.sorapointa.game.Player
import org.sorapointa.proto.PacketHeadOuterClass.PacketHead
import org.sorapointa.proto.SoraPacket
import org.sorapointa.proto.findCommonNameFromCmdId
import org.sorapointa.server.network.IncomingPacketFactories.handlePacket
import org.sorapointa.utils.*
import org.sorapointa.utils.crypto.MT19937
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

internal interface NetworkHandlerStateInterface : WithState<NetworkHandlerStateInterface.State> {

    enum class State {
        INITIALIZE,
        LOGIN, // Client haven't got the in-game key
        OK, // Client got the in-game key
        CLOSED
    }
}

internal open class NetworkHandler(
    private val connection: UkcpChannel,
    private val parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) {

    lateinit var bindPlayer: Player

    private val scope = ModuleScope(logger, "NetworkHandler[${getHost()}]", parentCoroutineContext)

    val dispatchKey: Deferred<ByteArray> = scope.async {
        newSuspendedTransaction {
            DispatchKeyData.getOrGenerate(getHost()).key
        }
    }

    var gameKey: ByteArray? = null

    val networkStateController by lazy {
        StateController(
            scope = scope,
            parentStateClass = this,
            Initialize(), Login(), OK(), Closed()
        )
    }

    suspend fun init() {
        bindPlayer = Player(this, parentCoroutineContext)
        bindPlayer.init()
        networkStateController.init()
    }

    fun getHost(): String =
        connection.host

    open suspend fun sendPacket(packet: OutgoingPacket, metadata: PacketHead? = null) {
        if (networkStateController.getCurrentState() == NetworkHandlerStateInterface.State.CLOSED) return
        scope.launch {
            SendOutgoingPacketEvent(bindPlayer, packet).broadcastEvent {
                if (metadata != null) {
                    packet.metadata = metadata
                }
                logger.debug { "Send: ${findCommonNameFromCmdId(packet.cmdId)} Id: ${packet.cmdId}" }
                connection.writeAndFlushOrCloseAsync(packet)
            }
        }
    }

    open suspend fun handlePacket(packet: SoraPacket) {
        if (networkStateController.getCurrentState() == NetworkHandlerStateInterface.State.CLOSED) return
        scope.launch {
            HandleRawSoraPacketEvent(bindPlayer, packet).broadcastEvent {
                with(bindPlayer) {
                    handlePacket(packet)?.also {
                        sendPacket(it)
                        AfterSendIncomingPacketResponseEvent(bindPlayer, it).broadcast()
                    }
                }
            }
        }
    }

    open suspend fun updateKeyWithSeed(keySeed: ULong): ByteArray =
        MT19937.generateKey(keySeed).also {
            gameKey = it
            networkStateController.setState(NetworkHandlerStateInterface.State.OK)
        }

    protected open fun setupConnectionPipeline() {
        connection.pipeline()
            .addLast(object : ChannelInboundHandlerAdapter() {
                @Deprecated("Deprecated in Java")
                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                    scope.launch {
                        networkStateController.setState(NetworkHandlerStateInterface.State.CLOSED)
                        throw cause
                    }
                }
            })
            .addLast(OutgoingPacketEncoder(this))
            .addLast(IncomingPacketDecoder(this))
            .addLast(object : SimpleChannelInboundHandler<SoraPacket>() {
                override fun channelRead0(ctx: ChannelHandlerContext, msg: SoraPacket) {
                    scope.launch {
                        handlePacket(msg)
                    }
                }
            })
        logger.debug { "Session [${connection.remoteAddress()}] has inited" }
    }

    inner class Initialize : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State = NetworkHandlerStateInterface.State.INITIALIZE

        override suspend fun startState() {
            setupConnectionPipeline()
            networkStateController.setState(NetworkHandlerStateInterface.State.LOGIN)
        }
    }

    inner class Login : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State = NetworkHandlerStateInterface.State.LOGIN
    }

    inner class OK : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State = NetworkHandlerStateInterface.State.OK
    }

    inner class Closed : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State = NetworkHandlerStateInterface.State.CLOSED
    }
}

internal class ConnectionInitializer(
    private val scope: ModuleScope
) : ChannelInitializer<UkcpChannel>() {
    private lateinit var networkHandler: NetworkHandler

    override fun initChannel(ch: UkcpChannel) {
        logger.info { "New session from [${ch.remoteAddress()}] connected" }
        networkHandler = NetworkHandler(ch, scope.coroutineContext)
        runBlocking {
            networkHandler.init()
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        scope.launch {
            logger.info { "Session [${ctx.channel().remoteAddress()}] has disconnected" }
            networkHandler.networkStateController
                .setState(NetworkHandlerStateInterface.State.CLOSED)
        }
    }
}

internal class OutgoingPacketEncoder(
    private val networkHandler: NetworkHandler
) :
    MessageToByteEncoder<OutgoingPacket>(OutgoingPacket::class.java) {

    override fun encode(ctx: ChannelHandlerContext, msg: OutgoingPacket, out: ByteBuf) {
        runBlocking {
            val key = networkHandler.gameKey ?: networkHandler.dispatchKey.await()
            out.writeBytes(msg.toFinalBytePacket().xor(key))
        }
    }
}

internal class IncomingPacketDecoder(
    private val networkHandler: NetworkHandler
) : MessageToMessageDecoder<ByteBuf>() {

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        runBlocking {
            val key = networkHandler.gameKey ?: networkHandler.dispatchKey.await()
            msg.readToSoraPacket(key) { out.add(it) }
        }
    }
}
