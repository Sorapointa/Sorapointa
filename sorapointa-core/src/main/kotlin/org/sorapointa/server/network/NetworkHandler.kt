package org.sorapointa.server.network

import io.jpower.kcp.netty.UkcpChannel
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.MessageToMessageDecoder
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.sorapointa.dispatch.DispatchServer
import org.sorapointa.event.StateController
import org.sorapointa.event.WithState
import org.sorapointa.game.Player
import org.sorapointa.proto.SoraPacket
import org.sorapointa.server.network.IncomingPacketFactories.handlePacket
import org.sorapointa.utils.ModuleScope
import org.sorapointa.utils.readToSoraPacket
import org.sorapointa.utils.writeAndFlushOrCloseAsync
import org.sorapointa.utils.xor
import java.net.InetSocketAddress
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

internal interface NetworkHandlerStateInterface : WithState<NetworkHandlerStateInterface.State> {

    fun getKey(): ByteArray

    enum class State {
        INITIALIZE,
        LOGIN, // Client haven't got the in-game key
        OK, // Client got the in-game key
        CLOSED
    }

}

internal open class NetworkHandler(
    private val connection: UkcpChannel,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) {

    private lateinit var bindPlayer: Player

    private val scope = ModuleScope(logger, "NetworkHandler[${connection.host}]", parentCoroutineContext)

    val networkStateController by lazy {
        StateController(
            scope = scope,
            parentStateClass = this,
            Initialize(), Login(), OK(), Closed()
        )
    }

    fun getHost(): String =
        connection.host

    open fun sendPacket(packet: OutgoingPacket) {
        if (networkStateController.getCurrentState() == NetworkHandlerStateInterface.State.CLOSED) return
        connection.writeAndFlushOrCloseAsync(packet)
    }

    open fun handlePacket(soraPacket: SoraPacket)  {
        if (networkStateController.getCurrentState() == NetworkHandlerStateInterface.State.CLOSED) return
        with(bindPlayer) {
            handlePacket(soraPacket)?.also {
                sendPacket(it)
            }
        }
    }

    open fun getKey(): ByteArray =
        networkStateController.getStateInstance().getKey()

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
                    handlePacket(msg)
                }
            })
    }

    private val Channel.host: String
        get() = (remoteAddress() as InetSocketAddress).address.hostAddress


    inner class Initialize : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State = NetworkHandlerStateInterface.State.INITIALIZE

        override suspend fun startState() {
            logger.info { "New session from [${connection.remoteAddress()}] connected" }
            setupConnectionPipeline()
            networkStateController.setState(NetworkHandlerStateInterface.State.LOGIN)
        }


        override fun getKey(): ByteArray =
            DispatchServer.dispatchKeyMap[getHost()]?.key ?: ByteArray(0)


    }

    inner class Login : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State = NetworkHandlerStateInterface.State.LOGIN

        override suspend fun startState() {
            /*
            C: GetPlayerTokenReq
            S: GetPlayerTokenRsp

            C: PlayerLoginReq

            S: OpenStateUpdateNotify
            S: StoreWeightLimitNotify
            S: PlayerStoreNotify
            S: AvatarDataNotify
            S: PlayerEnterSceneNotify

            S: PlayerLoginRsp

            C: GetPlayerSocialDetailReq
            S: GetPlayerSocialDetailRsp

            C: EnterSceneReadyReq
            S: EnterSceneReadyRsp

            C: SceneInitFinishReq

            S: EnterScenePeerNotify
            S: WorldDataNotify
            S: WorldPlayerInfoNotify
            S: ScenePlayerInfoNotify
            S: PlayerEnterSceneInfoNotify
            S: PlayerGameTimeNotify
            S: SceneTimeNotify
            S: SceneDataNotify
            S: HostPlayerNotify
            S: SceneTeamUpdateNotify

            S: SceneInitFinishRsp

            C: EnterSceneDoneReq
            S: SceneEntityAppearNotify
            S: EnterSceneDoneRsp

            C: PostEnterSceneReq
            S: PostEnterSceneRsp
             */
        }

        override fun getKey(): ByteArray =
            DispatchServer.dispatchKeyMap[getHost()]?.key ?: ByteArray(0)

    }

    inner class OK : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State = NetworkHandlerStateInterface.State.OK


        override fun getKey(): ByteArray {
            TODO("Not yet implemented")
        }

    }

    inner class Closed : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State = NetworkHandlerStateInterface.State.CLOSED

        override fun getKey(): ByteArray {
            TODO("Not yet implemented")
        }

    }


}

internal class ConnectionInitializer(
    private val scope: ModuleScope
) : ChannelInitializer<UkcpChannel>() {
    private lateinit var player: Player
    private lateinit var networkHandler: NetworkHandler

    override fun initChannel(ch: UkcpChannel) {
        networkHandler = NetworkHandler(ch, scope.coroutineContext)
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
        out.writeBytes(msg.toFinalBytePacket().xor(networkHandler.getKey()))
    }

}

internal class IncomingPacketDecoder(
    private val networkHandler: NetworkHandler
) : MessageToMessageDecoder<ByteBuf>() {

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        out.add(msg.readToSoraPacket(networkHandler.getKey()))
    }

}



