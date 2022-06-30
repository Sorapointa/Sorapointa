package org.sorapointa.server.network

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser
import org.sorapointa.event.broadcastEvent
import org.sorapointa.events.HandleIncomingPacketEvent
import org.sorapointa.events.HandlePreLoginIncomingPacketEvent
import org.sorapointa.game.Player
import org.sorapointa.proto.SoraPacket
import org.sorapointa.utils.uncheckedCast

interface IncomingPacketHandler<TPacketReq : GeneratedMessageV3> {

    val cmdId: UShort

    fun parsing(data: ByteArray): TPacketReq
}

/**
 * Server passively receives a packet (request),
 * client will require a response, copy metadata in this case
 */
internal abstract class AbstractIncomingPacketHandler
<TPacketReq : GeneratedMessageV3>(
    override val cmdId: UShort
) : IncomingPacketHandler<TPacketReq> {

    protected abstract val parser: Parser<TPacketReq>

    override fun parsing(data: ByteArray): TPacketReq = parser.parseFrom(data)
}

internal abstract class IncomingSessionPacketHandler
<TPacketReq : GeneratedMessageV3, TState : NetworkHandlerStateInterface>(
    cmdId: UShort
) : AbstractIncomingPacketHandler<TPacketReq>(cmdId) {
    abstract suspend fun TState.handle(soraPacket: SoraPacket): OutgoingPacket?
}

internal abstract class IncomingPlayerPacketHandler
<TPacketReq : GeneratedMessageV3>(
    cmdId: UShort
) : IncomingSessionPacketHandler<TPacketReq, NetworkHandler.PlayerHandlePacketState>(cmdId) {

    override suspend fun NetworkHandler.PlayerHandlePacketState.handle(
        soraPacket: SoraPacket
    ): OutgoingPacket? =
        this.bindPlayer.handle(soraPacket)

    abstract suspend fun Player.handle(soraPacket: SoraPacket): OutgoingPacket?
}

internal abstract class IncomingPacketHandlerWithoutResponse
<TPacketReq : GeneratedMessageV3>(
    cmdId: UShort
) : IncomingPlayerPacketHandler<TPacketReq>(cmdId) {

    protected abstract suspend fun Player.handlePacket(packet: TPacketReq)

    override suspend fun Player.handle(soraPacket: SoraPacket): OutgoingPacket? {
        val packet = parsing(soraPacket.data)
        HandleIncomingPacketEvent(this, packet).broadcastEvent {
            handlePacket(packet)
        }
        return null // No response for this type of packet
    }
}

internal abstract class IncomingPacketHandlerWithResponse
<TPacketReq : GeneratedMessageV3, TPacketRsp : OutgoingPacket>(
    cmdId: UShort
) : IncomingPlayerPacketHandler<TPacketReq>(cmdId) {

    protected abstract suspend fun Player.handlePacket(packet: TPacketReq): TPacketRsp

    override suspend fun Player.handle(soraPacket: SoraPacket): OutgoingPacket? {
        val packet = parsing(soraPacket.data)
        return HandleIncomingPacketEvent(this, packet).broadcastEvent {
            handlePacket(packet).also {
                it.metadata = soraPacket.metadata
            }
        }
    }
}

internal abstract class IncomingPreLoginPacketHandler
<TPacketReq : GeneratedMessageV3, TPacketRsp : OutgoingPacket, TState : NetworkHandlerStateInterface>(
    cmdId: UShort
) : IncomingSessionPacketHandler<TPacketReq, TState>(cmdId) {

    protected abstract suspend fun TState.handlePacket(packet: TPacketReq): TPacketRsp

    override suspend fun TState.handle(soraPacket: SoraPacket): OutgoingPacket? {
        val packet = parsing(soraPacket.data)
        return HandlePreLoginIncomingPacketEvent(networkHandler, packet).broadcastEvent {
            handlePacket(packet).also {
                it.metadata = soraPacket.metadata
            }
        }
    }
}

internal object IncomingPacketFactory {

    // TODO: Check it type in real situation
    private val incomingPacketHandlers = listOf<IncomingSessionPacketHandler<*, *>>(
        GetPlayerTokenReqHandler,
        PingReqHandler,
        PlayerSetPauseReqHandler,
        PlayerLoginReqHandler,
        SetPlayerBornDataReqHandler,
        GetPlayerSocialDetailReqHandler,
        EnterSceneReadyReqHandler,
        SceneInitFinishReqHandler,
        UnionCmdNotifyHandler
    )

    suspend inline fun <reified TState : NetworkHandlerStateInterface> TState.tryHandle(
        packet: SoraPacket
    ): OutgoingPacket? {
        return incomingPacketHandlers.firstOrNull { it.cmdId == packet.cmdId }
            ?.let {
                it.uncheckedCast<IncomingSessionPacketHandler<*, TState>>().run {
                    handle(packet)
                }
            }
    }
}

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

/*

NetworkHandler (ByteReadPacket) -> IncomingFactories (SoraPacket, find Factory by cmdId) [List Factory]
  -> IncomingFactory (get Serializer of ProtobufData, then parse it)
  -> SomeIncomingPacketFactory (get ProtoBuf only)
  -> may return a (Reponse OutgoingPacket)
  -> NetworkHandler would send it
 */
