package org.sorapointa.server.network

import io.jpower.kcp.netty.UkcpChannel
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.MessageToMessageDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.sorapointa.dispatch.data.DispatchKeyData
import org.sorapointa.event.StateController
import org.sorapointa.event.WithState
import org.sorapointa.game.Player
import org.sorapointa.proto.PacketHeadOuterClass.PacketHead
import org.sorapointa.proto.SoraPacket
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
    dispatchKey: ByteArray,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) {

    private lateinit var bindPlayer: Player

    private val scope = ModuleScope(logger, "NetworkHandler[${getHost()}]", parentCoroutineContext)

    var key: ByteArray = dispatchKey
        private set


    val networkStateController by lazy {
        StateController(
            scope = scope,
            parentStateClass = this,
            Initialize(), Login(), OK(), Closed()
        )
    }

    fun getHost(): String =
        connection.host

    open fun sendPacket(packet: OutgoingPacket, metadata: PacketHead? = null) {
        if (networkStateController.getCurrentState() == NetworkHandlerStateInterface.State.CLOSED) return
        if (metadata != null) {
            packet.metadata = metadata
        }
        connection.writeAndFlushOrCloseAsync(packet)
    }

    open suspend fun handlePacket(soraPacket: SoraPacket) {
        if (networkStateController.getCurrentState() == NetworkHandlerStateInterface.State.CLOSED) return
        with(bindPlayer) {
            handlePacket(soraPacket)?.also {
                sendPacket(it)
            }
        }
    }

    open suspend fun updateKeyWithSeed(keySeed: ULong): ByteArray =
        withContext(Dispatchers.Default) {
            MT19937.generateKey(keySeed).also {
                key = it
                networkStateController.setState(NetworkHandlerStateInterface.State.OK)
            }
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
    }

    inner class Initialize : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State = NetworkHandlerStateInterface.State.INITIALIZE

        override suspend fun startState() {
            logger.info { "New session from [${connection.remoteAddress()}] connected" }
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
    private lateinit var player: Player
    private lateinit var networkHandler: NetworkHandler

    override fun initChannel(ch: UkcpChannel) {
        val key = runBlocking {
            newSuspendedTransaction {
                DispatchKeyData.getOrGenerate(ch.host).key
            }
        }
        networkHandler = NetworkHandler(ch, key, scope.coroutineContext)
        player = Player(networkHandler, scope.coroutineContext)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        scope.launch {
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
        out.writeBytes(msg.toFinalBytePacket().xor(networkHandler.key))
    }
}

internal class IncomingPacketDecoder(
    private val networkHandler: NetworkHandler
) : MessageToMessageDecoder<ByteBuf>() {

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        out.add(msg.readToSoraPacket(networkHandler.key))
    }
}
