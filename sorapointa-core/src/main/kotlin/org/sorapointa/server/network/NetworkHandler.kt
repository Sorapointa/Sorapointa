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
import org.sorapointa.Sorapointa
import org.sorapointa.SorapointaConfig
import org.sorapointa.dispatch.data.Account
import org.sorapointa.dispatch.data.DispatchKeyData
import org.sorapointa.event.*
import org.sorapointa.events.SendOutgoingPacketEvent
import org.sorapointa.game.Player
import org.sorapointa.game.data.PlayerData
import org.sorapointa.proto.PacketHeadOuterClass.PacketHead
import org.sorapointa.proto.SoraPacket
import org.sorapointa.proto.findCommonNameFromCmdId
import org.sorapointa.server.network.IncomingPacketFactories.handlePlayerPacket
import org.sorapointa.server.network.IncomingPacketFactories.handleSessionPacket
import org.sorapointa.utils.*
import org.sorapointa.utils.crypto.MT19937
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

internal interface NetworkHandlerStateInterface : WithState<NetworkHandlerStateInterface.State> {

    suspend fun handlePacket(packet: SoraPacket)

    enum class State {
        LOGIN, // Client haven't got the in-game key
        OK, // Client got the in-game key
        CLOSED
    }
}

internal open class NetworkHandler(
    private val connection: UkcpChannel,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) {

//    var bindPlayer: Player? = null

    val scope = ModuleScope("NetworkHandler[${getHost()}]", parentCoroutineContext)

    val networkStateController by lazy {
        StateController<NetworkHandlerStateInterface.State, NetworkHandlerStateInterface, NetworkHandler>(
            scope = scope,
            parentStateClass = this,
            Login(this)
        )
    }

    var clientTime: Int = 0
        private set

    private var lastPingTime = now()

    internal suspend fun init() {
        networkStateController.init()
        scope.launch {
            while (isActive) {
                delay(1000)
                if (now() - lastPingTime > SorapointaConfig.data.networkSetting.pingTimeout) {
                    close()
                }
            }
        }
    }

    internal suspend fun close() {
        if (connection.isActive) {
            connection.close()
        }
        networkStateController.setState(Closed())
        scope.dispose()
    }

    fun getHost(): String =
        connection.host

    suspend fun getKey(): ByteArray? =
        when (val state = networkStateController.getStateInstance()) {
            is NetworkHandler.Login -> state.dispatchKey.await()
            is NetworkHandler.OK -> state.gameKey
            else -> null
        }

    fun updatePingTime(clientTime: Int) {
        this.clientTime = clientTime
        lastPingTime = now()
    }

    open suspend fun sendPacket(packet: OutgoingPacket, metadata: PacketHead? = null): Job? {
        if (networkStateController.getCurrentState() == NetworkHandlerStateInterface.State.CLOSED) return null
        return scope.launch {
            SendOutgoingPacketEvent(this@NetworkHandler, packet).broadcastEvent {
                if (metadata != null) {
                    packet.metadata = metadata
                }
                logger.debug { "Send: ${findCommonNameFromCmdId(packet.cmdId)} Id: ${packet.cmdId}" }
                connection.writeAndFlushOrCloseAsync(packet)
            }
        }
    }

    open suspend fun handlePacket(packet: SoraPacket) {
        scope.launch {
            logger.debug { "Recv: ${findCommonNameFromCmdId(packet.cmdId)} Id: ${packet.cmdId}" }
            networkStateController.getStateInstance().handlePacket(packet)
        }
    }

    protected open fun setupConnectionPipeline() {
        connection.pipeline()
            .addLast(object : ChannelInboundHandlerAdapter() {
                @Deprecated("Deprecated in Java")
                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                    scope.launch {
                        close()
                        throw cause
                    }
                }

                override fun channelInactive(ctx: ChannelHandlerContext) {
                    logger.info { "Session [${ctx.channel().remoteAddress()}] has disconnected" }
                    scope.launch {
                        close()
                    }
                }
            })
            .addLast("encoder", OutgoingPacketEncoder(this))
            .addLast("decoder", IncomingPacketDecoder(this))
            .addLast(object : SimpleChannelInboundHandler<SoraPacket>() {
                override fun channelRead0(ctx: ChannelHandlerContext, msg: SoraPacket) {
                    scope.launch {
                        handlePacket(msg)
                    }
                }
            })
        logger.debug { "Session [${connection.remoteAddress()}] has inited" }
    }

    inner class Login(
        private val networkHandler: NetworkHandler
    ) : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State =
            NetworkHandlerStateInterface.State.LOGIN

        private var updateKeySeed: (suspend () -> Unit)? = null

        val dispatchKey: Deferred<ByteArray> = scope.async {
            newSuspendedTransaction {
                DispatchKeyData.getOrGenerate(getHost()).key
            }
        }

        override suspend fun startState() {
            setupConnectionPipeline()
        }

        suspend fun updateKeyAndBindPlayer(account: Account, seed: ULong) {

            val player = Player(
                account = account,
                data = PlayerData.findOrCreate(account.id.value),
                networkHandler = networkHandler,
                parentCoroutineContext = scope.coroutineContext
            )
            player.init()

            Sorapointa.playerList.add(player)

            updateKeySeed = {
                val gameKey = MT19937.generateKey(seed)
                networkStateController.setState(OK(gameKey, player))
            }
        }

        override suspend fun handlePacket(packet: SoraPacket) {
            handleSessionPacket(packet)?.also {
                sendPacket(it)?.join() // Wait for sending, cuz we need to update key
                updateKeySeed?.let {
                    it()
                }
            }
        }
    }

    inner class OK(
        val gameKey: ByteArray,
        val bindPlayer: Player
    ) : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State = NetworkHandlerStateInterface.State.OK

        override suspend fun handlePacket(packet: SoraPacket) {
            with(bindPlayer) {
                handlePlayerPacket(packet)?.also {
                    sendPacket(it)
                }
            }
        }
    }

    inner class Closed : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State = NetworkHandlerStateInterface.State.CLOSED

        override suspend fun handlePacket(packet: SoraPacket) {
            // ignore
        }
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
}

internal class OutgoingPacketEncoder(
    private val networkHandler: NetworkHandler
) :
    MessageToByteEncoder<OutgoingPacket>(OutgoingPacket::class.java) {

    override fun encode(ctx: ChannelHandlerContext, msg: OutgoingPacket, out: ByteBuf) {
        runBlocking {
            networkHandler.getKey()?.let { key ->
                out.writeBytes(msg.toFinalBytePacket().xor(key))
            }
        }
    }
}

internal class IncomingPacketDecoder(
    private val networkHandler: NetworkHandler
) : MessageToMessageDecoder<ByteBuf>() {

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        runBlocking {
            networkHandler.getKey()?.let { key ->
                msg.readToSoraPacket(key) { out.add(it) }
            }
        }
    }
}
