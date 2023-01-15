package org.sorapointa.server.network

import com.squareup.wire.Message
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import kcp.highway.KcpListener
import kcp.highway.Ukcp
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.sorapointa.Sorapointa
import org.sorapointa.SorapointaConfig
import org.sorapointa.dispatch.data.DispatchKeyData
import org.sorapointa.event.StateController
import org.sorapointa.event.WithState
import org.sorapointa.event.broadcastEvent
import org.sorapointa.events.SendPacketEvent
import org.sorapointa.game.Player
import org.sorapointa.game.PlayerImpl
import org.sorapointa.game.data.PlayerData
import org.sorapointa.game.impl
import org.sorapointa.proto.PacketHead
import org.sorapointa.proto.SoraPacket
import org.sorapointa.proto.toJson
import org.sorapointa.server.network.IncomingPacketFactory.handle
import org.sorapointa.utils.*
import org.sorapointa.utils.crypto.MT19937
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

internal interface NetworkHandlerStateI : WithState<NetworkHandlerStateI.State> {

    val networkHandler: NetworkHandler

    suspend fun handlePacket(packet: SoraPacket)

    enum class State {
        WAIT_TOKEN, // Client hasn't got the in-game key
        LOGIN,
        OK, // Client got the in-game key
        CLOSED,
    }
}

internal open class NetworkHandler(
    private val connection: Ukcp,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) {

    val host: String = connection.user().remoteAddress.address.hostAddress

    val state by lazy {
        StateController<_, NetworkHandlerStateI, _>(
            scope = scope,
            parentStateClass = this,
            WaitToken(this),
        )
    }

    val clientTime
        get() = _clientTime.value

    private val scope = ModuleScope("NetworkHandler[$host]", parentCoroutineContext)

    private val _clientTime = atomic(0)

    private var lastPingTime = now()

    internal suspend fun init() {
        state.init()
        scope.launch {
            while (isActive) {
                delay(1000)
                if (now() - lastPingTime > SorapointaConfig.data.networkSetting.pingTimeout) {
                    logger.debug { "Session $host has been closed due to timeout" }
                    close()
                }
            }
        }
    }

    internal suspend fun close() {
        sendPacketSync(ServerDisconnectClientNotifyPacket())
        if (connection.isActive) {
            connection.close()
        }
        state.setState(Closed(this))
        scope.dispose()
    }

    suspend fun getKey(): ByteArray? =
        when (val state = state.getStateInstance()) {
            is WaitToken -> state.dispatchKey.await()
            is UpdatedKeyState -> state.gameKey
            else -> null
        }

    fun updatePingTime(clientTime: Int) {
        _clientTime.value = clientTime
        lastPingTime = now()
    }

    open fun <T : Message<*, *>> sendPacket(
        packet: OutgoingPacket<T>,
        metadata: PacketHead? = null,
    ): Job = scope.launch {
        sendPacketSync(packet, metadata)
    }

    open suspend fun <T : Message<*, *>> sendPacketSync(
        packet: OutgoingPacket<T>,
        metadata: PacketHead? = null,
    ) {
        if (state.getCurrentState() == NetworkHandlerStateI.State.CLOSED) return
        SendPacketEvent(this, packet).broadcastEvent {
            if (metadata != null) {
                packet.metadata = metadata
            }
            val proto = packet.buildProto()
            logPacket(isOutgoing = true, cmdId = packet.cmdId, metadata = metadata, proto = proto)
            val bytes = getKey()?.let { key ->
                packet.toFinalBytePacket(proto).xor(key)
            }
            val buf: ByteBuf = Unpooled.wrappedBuffer(bytes)
            connection.write(buf)
            buf.release()
        }
    }

    open fun handlePacket(packet: SoraPacket): Job =
        scope.launch {
            if (logger.isDebugEnabled) {
                logPacket(
                    isOutgoing = false,
                    cmdId = packet.cmdId,
                    metadata = packet.metadata,
                    proto = IncomingPacketFactory.parseToProto(packet),
                )
            }
            runCatching {
                state.getStateInstance().handlePacket(packet)
            }.getOrElse {
                throw IllegalStateException(
                    "Error while handling packet: ${findCommonNameFromCmdId(packet.cmdId)}",
                    it,
                )
            }
        }

    private fun logPacket(
        isOutgoing: Boolean,
        cmdId: UShort,
        metadata: PacketHead?,
        proto: Message<*, *>?,
    ) {
        if (logger.isDebugEnabled) {
            val setting = SorapointaConfig.data.debugSetting
            val cmdName = findCommonNameFromCmdId(cmdId)
            val switch = setting.blockListPacketWatcher
            if (switch) {
                if (setting.blocklist.contains(cmdName)) return
            } else {
                if (!setting.allowlist.contains(cmdName)) return
            }
            val direction = if (isOutgoing) "Send" else "Recv"
            logger.debug(
                "SEQ: ${metadata?.client_sequence_id ?: 0} " +
                    "$direction: $cmdName " +
                    (proto?.toJson()?.let { "Body: $it" } ?: ""),
            )
        }
    }

    abstract inner class UpdatedKeyState : NetworkHandlerStateI {
        abstract val gameKey: ByteArray
    }

    abstract inner class PreLogin : UpdatedKeyState() {

        override suspend fun handlePacket(packet: SoraPacket) {
            newSuspendedTransaction {
                handle(PacketHandlerContext(this@PreLogin, packet.metadata), packet)
            }
        }
    }

    abstract inner class Logged : PreLogin() {
        abstract val player: Player

        override suspend fun handlePacket(packet: SoraPacket) {
            newSuspendedTransaction {
                handle(PlayerPacketHandlerContext(player, packet.metadata), packet)
            }
        }
    }

    inner class WaitToken(
        override val networkHandler: NetworkHandler,
    ) : NetworkHandlerStateI {

        override val state: NetworkHandlerStateI.State =
            NetworkHandlerStateI.State.WAIT_TOKEN

        private var updateSessionState: NetworkHandlerStateI? = null

        val dispatchKey: Deferred<ByteArray> = scope.async {
            newSuspendedTransaction {
                DispatchKeyData.getOrGenerate(host).key
            }
        }

        suspend fun updateKeyAndBindPlayer(uid: Int, seed: ULong) {
            val playerData = PlayerData.findById(uid)
            val gameKey = MT19937.generateKey(seed)

            updateSessionState = Login(uid, playerData, gameKey, networkHandler)
        }

        override suspend fun handlePacket(packet: SoraPacket) {
            newSuspendedTransaction {
                handle(PacketHandlerContext(this@WaitToken, packet.metadata), packet)
                updateSessionState?.let { this@NetworkHandler.state.setState(it) }
            }
        }
    }

    inner class Login(
        val uid: Int,
        val playerData: PlayerData?,
        override val gameKey: ByteArray,
        override val networkHandler: NetworkHandler,
    ) : PreLogin() {

        override val state: NetworkHandlerStateI.State =
            NetworkHandlerStateI.State.LOGIN

        suspend fun createPlayer(playerData: PlayerData): Player {
            val player = PlayerImpl(
                uid = uid,
                data = playerData,
                networkHandler = networkHandler,
                parentCoroutineContext = networkHandler.scope.coroutineContext,
            )
            Sorapointa.addPlayer(player)
            return player
        }

        suspend fun setToOK(player: Player) {
            this@NetworkHandler.state.setState(OK(gameKey, player, networkHandler))
            val state = player.impl().state.getStateInstance()
            if (state is PlayerImpl.Login) {
                state.onLogin()
            }
        }
    }

    inner class OK(
        override val gameKey: ByteArray,
        override val player: Player,
        override val networkHandler: NetworkHandler,
    ) : Logged() {

        override val state: NetworkHandlerStateI.State =
            NetworkHandlerStateI.State.OK
    }

    inner class Closed(
        override val networkHandler: NetworkHandler,
    ) : NetworkHandlerStateI {

        override val state: NetworkHandlerStateI.State =
            NetworkHandlerStateI.State.CLOSED

        override suspend fun handlePacket(packet: SoraPacket) {
            // ignore
        }
    }
}

// F**K KCP Lib
private val connectionMap = ConcurrentHashMap<Ukcp, NetworkHandler>()

// NO RUNTIME STATE of this class
internal class ConnectionListener(
    private val scope: ModuleScope,
) : KcpListener {

    override fun onConnected(ukcp: Ukcp) {
        logger.info { "New session from [${ukcp.user().remoteAddress}] connected" }
        val handler = NetworkHandler(ukcp, scope.coroutineContext)
        connectionMap[ukcp] = handler
        runBlocking {
            handler.init()
        }
    }

    override fun handleReceive(byteBuf: ByteBuf, ukcp: Ukcp) {
        runBlocking {
            val handler = connectionMap[ukcp]
            val key = handler?.getKey() ?: return@runBlocking
            byteBuf.readToSoraPacket(key) {
                handler.handlePacket(it)
            }
        }
    }

    override fun handleException(throwable: Throwable, ukcp: Ukcp) {
        scope.launch {
            connectionMap[ukcp]?.close()
            connectionMap.remove(ukcp)
            throw throwable
        }
    }

    override fun handleClose(ukcp: Ukcp) {
        logger.info { "Session [${ukcp.user().remoteAddress}] has disconnected" }
        scope.launch {
            connectionMap[ukcp]?.close()
            connectionMap.remove(ukcp)
        }
    }
}
