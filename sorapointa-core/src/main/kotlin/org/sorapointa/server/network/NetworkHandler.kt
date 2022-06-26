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
import org.sorapointa.event.StateController
import org.sorapointa.event.WithState
import org.sorapointa.event.broadcastEvent
import org.sorapointa.events.SendOutgoingPacketEvent
import org.sorapointa.game.Player
import org.sorapointa.game.PlayerImpl
import org.sorapointa.game.data.PlayerData
import org.sorapointa.game.data.PlayerDataImpl
import org.sorapointa.game.impl
import org.sorapointa.proto.PacketHeadOuterClass.PacketHead
import org.sorapointa.proto.SoraPacket
import org.sorapointa.proto.findCommonNameFromCmdId
import org.sorapointa.server.network.IncomingPacketFactory.tryHandle
import org.sorapointa.utils.*
import org.sorapointa.utils.crypto.MT19937
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

internal interface NetworkHandlerStateInterface : WithState<NetworkHandlerStateInterface.State> {

    val networkHandler: NetworkHandler

    suspend fun handlePacket(packet: SoraPacket)

    enum class State {
        WAIT_TOKEN, // Client hasn't got the in-game key
        LOGIN,
        OK, // Client got the in-game key
        CLOSED
    }
}

internal open class NetworkHandler(
    private val connection: UkcpChannel,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) {

    private val scope = ModuleScope("NetworkHandler[${getHost()}]", parentCoroutineContext)

    val networkStateController by lazy {
        StateController<_, NetworkHandlerStateInterface, _>(
            scope = scope,
            parentStateClass = this,
            WaitToken(this)
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
        networkStateController.setState(Closed(this))
        scope.dispose()
    }

    fun getHost(): String =
        connection.host

    suspend fun getKey(): ByteArray? =
        when (val state = networkStateController.getStateInstance()) {
            is WaitToken -> state.dispatchKey.await()
            is UpdatedKeyState -> state.gameKey
            else -> null
        }

    fun updatePingTime(clientTime: Int) {
        this.clientTime = clientTime
        lastPingTime = now()
    }

    open fun sendPacket(
        packet: OutgoingPacket,
        metadata: PacketHead? = null
    ): Job = scope.launch {
        if (networkStateController.getCurrentState() == NetworkHandlerStateInterface.State.CLOSED) return@launch
        SendOutgoingPacketEvent(this@NetworkHandler, packet).broadcastEvent {
            if (metadata != null) {
                packet.metadata = metadata
            }
            logger.debug { "Send: ${findCommonNameFromCmdId(packet.cmdId)} Id: ${packet.cmdId}" }
            connection.writeAndFlushOrCloseAsync(packet)
        }
    }

    open fun handlePacket(packet: SoraPacket): Job =
        scope.launch {
            logger.debug { "Recv: ${findCommonNameFromCmdId(packet.cmdId)} Id: ${packet.cmdId}" }
            networkStateController.getStateInstance().handlePacket(packet)
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
                    handlePacket(msg)
                }
            })
        logger.debug { "Session [${connection.remoteAddress()}] has inited" }
    }

    abstract inner class UpdatedKeyState : NetworkHandlerStateInterface {
        abstract val gameKey: ByteArray
    }

    abstract inner class SessionHandlePacketState : UpdatedKeyState() {

        override suspend fun handlePacket(packet: SoraPacket) {
            tryHandle(packet)?.also {
                sendPacket(it)
            }
        }
    }

    abstract inner class PlayerHandlePacketState : SessionHandlePacketState() {
        abstract val bindPlayer: Player
    }

    inner class WaitToken(
        override val networkHandler: NetworkHandler
    ) : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State =
            NetworkHandlerStateInterface.State.WAIT_TOKEN

        private var updateSessionState: NetworkHandlerStateInterface? = null

        val dispatchKey: Deferred<ByteArray> = scope.async {
            newSuspendedTransaction {
                DispatchKeyData.getOrGenerate(getHost()).key
            }
        }

        override suspend fun startState() {
            setupConnectionPipeline()
        }

        suspend fun updateKeyAndBindPlayer(account: Account, seed: ULong) {

            val playerData = newSuspendedTransaction {
                PlayerDataImpl.findById(account.id.value)
            }
            val gameKey = MT19937.generateKey(seed)

            updateSessionState = Login(account, playerData, gameKey, networkHandler)
        }

        override suspend fun handlePacket(packet: SoraPacket) {
            tryHandle(packet)?.also { outgoingPacket ->
                sendPacket(outgoingPacket).join() // Wait for sending, cuz we need to update key
                updateSessionState?.also { networkStateController.setState(it) }
            }
        }
    }

    inner class Login(
        val account: Account,
        val playerData: PlayerData?,
        override val gameKey: ByteArray,
        override val networkHandler: NetworkHandler
    ) : SessionHandlePacketState() {

        override val state: NetworkHandlerStateInterface.State =
            NetworkHandlerStateInterface.State.LOGIN

        suspend fun createPlayer(playerData: PlayerData) =
            PlayerImpl(
                account = account,
                data = playerData,
                networkHandler = networkHandler,
                parentCoroutineContext = networkHandler.scope.coroutineContext
            ).apply {
                Sorapointa.addPlayer(this)
            }

        suspend fun setToOK(player: Player) {
            networkStateController.setState(OK(gameKey, player, networkHandler))
            player.impl().state.getStateInstance().let {
                if (it is PlayerImpl.Login) {
                    it.onLogin()
                }
            }
        }
    }

    inner class OK(
        override val gameKey: ByteArray,
        override val bindPlayer: Player,
        override val networkHandler: NetworkHandler
    ) : PlayerHandlePacketState() {

        override val state: NetworkHandlerStateInterface.State =
            NetworkHandlerStateInterface.State.OK
    }

    inner class Closed(
        override val networkHandler: NetworkHandler
    ) : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State =
            NetworkHandlerStateInterface.State.CLOSED

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
) : MessageToByteEncoder<OutgoingPacket>(OutgoingPacket::class.java) {

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
