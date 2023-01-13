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
import org.sorapointa.dispatch.data.Account
import org.sorapointa.dispatch.data.DispatchKeyData
import org.sorapointa.event.StateController
import org.sorapointa.event.WithState
import org.sorapointa.event.broadcastEvent
import org.sorapointa.events.SendOutgoingPacketEvent
import org.sorapointa.game.Player
import org.sorapointa.game.PlayerImpl
import org.sorapointa.game.data.PlayerData
import org.sorapointa.game.impl
import org.sorapointa.proto.PacketHead
import org.sorapointa.proto.SoraPacket
import org.sorapointa.proto.toJson
import org.sorapointa.server.network.IncomingPacketFactory.tryHandle
import org.sorapointa.utils.*
import org.sorapointa.utils.crypto.MT19937
import java.util.concurrent.ConcurrentHashMap
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
        CLOSED,
    }
}

internal open class NetworkHandler(
    private val connection: Ukcp,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) {

    val host: String = connection.user().remoteAddress.address.hostAddress

    val networkStateController by lazy {
        StateController<_, NetworkHandlerStateInterface, _>(
            scope = scope,
            parentStateClass = this,
            WaitToken(this),
        )
    }

    val clientTime
        get() = _clientTime.value

    private val scope = ModuleScope("NetworkHandler[$host]", parentCoroutineContext)

    private val _clientTime = atomic(0)

    private val _packetSequence = atomic(0uL)

    private var lastPingTime = now()

    internal suspend fun init() {
        networkStateController.init()
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
        sendPacket(ServerDisconnectClientNotifyPacket())
        if (connection.isActive) {
            connection.close()
        }
        networkStateController.setState(Closed(this))
        scope.dispose()
    }

    suspend fun getKey(): ByteArray? =
        when (val state = networkStateController.getStateInstance()) {
            is WaitToken -> state.dispatchKey.await()
            is UpdatedKeyState -> state.gameKey
            else -> null
        }

    fun updatePingTime(clientTime: Int) {
        _clientTime.value = clientTime
        lastPingTime = now()
    }

    open fun <T : Message<*, *>> sendPacketAsync(
        packet: OutgoingPacket<T>,
        metadata: PacketHead? = null,
    ): Job = scope.launch {
        sendPacket(packet, metadata)
    }

    open suspend fun <T : Message<*, *>> sendPacket(
        packet: OutgoingPacket<T>,
        metadata: PacketHead? = null,
    ) {
        if (networkStateController.getCurrentState() == NetworkHandlerStateInterface.State.CLOSED) return
        SendOutgoingPacketEvent(this@NetworkHandler, packet).broadcastEvent {
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
            networkStateController.getStateInstance().handlePacket(packet)
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

    abstract inner class UpdatedKeyState : NetworkHandlerStateInterface {
        abstract val gameKey: ByteArray
    }

    abstract inner class SessionHandlePacketState : UpdatedKeyState() {

        override suspend fun handlePacket(packet: SoraPacket) {
            newSuspendedTransaction {
                tryHandle(packet)?.also {
                    sendPacket(it)
                }
            }
        }
    }

    abstract inner class PlayerHandlePacketState : SessionHandlePacketState() {
        abstract val bindPlayer: Player
    }

    inner class WaitToken(
        override val networkHandler: NetworkHandler,
    ) : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State =
            NetworkHandlerStateInterface.State.WAIT_TOKEN

        private var updateSessionState: NetworkHandlerStateInterface? = null

        val dispatchKey: Deferred<ByteArray> = scope.async {
            newSuspendedTransaction {
                DispatchKeyData.getOrGenerate(host).key
            }
        }

        suspend fun updateKeyAndBindPlayer(account: Account, seed: ULong) {
            val playerData = PlayerData.findById(account.id.value)
            val gameKey = MT19937.generateKey(seed)

            updateSessionState = Login(account, playerData, gameKey, networkHandler)
        }

        override suspend fun handlePacket(packet: SoraPacket) {
            newSuspendedTransaction {
                val outgoingPacket = tryHandle(packet) ?: return@newSuspendedTransaction
                sendPacket(outgoingPacket) // Wait for sending, cuz we need to update key
                updateSessionState?.also { networkStateController.setState(it) }
            }
        }
    }

    inner class Login(
        val account: Account,
        val playerData: PlayerData?,
        override val gameKey: ByteArray,
        override val networkHandler: NetworkHandler,
    ) : SessionHandlePacketState() {

        override val state: NetworkHandlerStateInterface.State =
            NetworkHandlerStateInterface.State.LOGIN

        suspend fun createPlayer(playerData: PlayerData): Player {
            val player = PlayerImpl(
                account = account,
                data = playerData,
                networkHandler = networkHandler,
                parentCoroutineContext = networkHandler.scope.coroutineContext,
            )
            Sorapointa.addPlayer(player)
            return player
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
        override val networkHandler: NetworkHandler,
    ) : PlayerHandlePacketState() {

        override val state: NetworkHandlerStateInterface.State =
            NetworkHandlerStateInterface.State.OK
    }

    inner class Closed(
        override val networkHandler: NetworkHandler,
    ) : NetworkHandlerStateInterface {

        override val state: NetworkHandlerStateInterface.State =
            NetworkHandlerStateInterface.State.CLOSED

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
            connectionMap[ukcp]?.let { handler ->
                handler.getKey()?.let { key ->
                    byteBuf.readToSoraPacket(key) {
                        handler.handlePacket(it)
                    }
                }
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
