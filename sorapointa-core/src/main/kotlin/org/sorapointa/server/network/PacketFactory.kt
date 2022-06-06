package org.sorapointa.server.network

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.sorapointa.dispatch.data.Account
import org.sorapointa.dispatch.plugins.currentRegionRsp
import org.sorapointa.event.broadcastEvent
import org.sorapointa.events.HandleIncomingPacketEvent
import org.sorapointa.events.HandlePreLoginIncomingPacketEvent
import org.sorapointa.game.Player
import org.sorapointa.proto.*
import org.sorapointa.proto.GetPlayerTokenReqOuterClass.GetPlayerTokenReq
import org.sorapointa.proto.PingReqOuterClass.PingReq
import org.sorapointa.proto.PlayerLoginReqOuterClass.PlayerLoginReq
import org.sorapointa.proto.RetcodeOuterClass.Retcode
import org.sorapointa.utils.randomULong

/**
 * Server passively receives a packet (request),
 * client will require a response, copy metadata in this case
 */
internal abstract class IncomingPacketFactory
<TPacketReq : GeneratedMessageV3>(
    val cmdId: UShort
) {

    protected abstract val parser: Parser<TPacketReq>

    fun parsing(data: ByteArray): TPacketReq = parser.parseFrom(data)
}

internal abstract class IncomingPlayerPacketFactory
<TPacketReq : GeneratedMessageV3>(
    cmdId: UShort
) : IncomingPacketFactory<TPacketReq>(cmdId) {
    abstract suspend fun Player.handle(soraPacket: SoraPacket): OutgoingPacket?
}

internal abstract class IncomingSessionPacketFactory
<TPacketReq : GeneratedMessageV3>(
    cmdId: UShort
) : IncomingPacketFactory<TPacketReq>(cmdId) {
    abstract suspend fun NetworkHandler.handle(soraPacket: SoraPacket): OutgoingPacket?
}

internal abstract class IncomingPacketFactoryWithoutResponse
<TPacketReq : GeneratedMessageV3>(
    cmdId: UShort
) : IncomingPlayerPacketFactory<TPacketReq>(cmdId) {

    protected abstract suspend fun Player.handlePacket(packet: TPacketReq)

    override suspend fun Player.handle(soraPacket: SoraPacket): OutgoingPacket? {
        val packet = parsing(soraPacket.data)
        HandleIncomingPacketEvent(this, packet).broadcastEvent {
            handlePacket(packet)
        }
        return null // No response for this type of packet
    }
}

internal abstract class IncomingPacketFactoryWithResponse
<TPacketReq : GeneratedMessageV3, TPacketRsp : OutgoingPacket>(
    cmdId: UShort
) : IncomingPlayerPacketFactory<TPacketReq>(cmdId) {

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

internal abstract class IncomingPreLoginPacketFactory
<TPacketReq : GeneratedMessageV3, TPacketRsp : OutgoingPacket>(
    cmdId: UShort
) : IncomingSessionPacketFactory<TPacketReq>(cmdId) {

    protected abstract suspend fun NetworkHandler.handlePacket(packet: TPacketReq): TPacketRsp

    override suspend fun NetworkHandler.handle(soraPacket: SoraPacket): OutgoingPacket? {
        val packet = parsing(soraPacket.data)
        return HandlePreLoginIncomingPacketEvent(this, packet).broadcastEvent {
            handlePacket(packet).also {
                it.metadata = soraPacket.metadata
            }
        }
    }
}

internal object IncomingPacketFactories {

    private val incomingSessionPacketFactories = listOf<IncomingSessionPacketFactory<*>>(
        GetPlayerTokenReqFactory,
        PingReqFactory
    )

    private val incomingPlayerPacketFactory = listOf<IncomingPlayerPacketFactory<*>>()

    suspend fun NetworkHandler.handleSessionPacket(packet: SoraPacket): OutgoingPacket? {
        return incomingSessionPacketFactories.firstOrNull { it.cmdId == packet.cmdId }
            ?.run {
                handle(packet)
            }
    }

    suspend fun Player.handlePlayerPacket(packet: SoraPacket): OutgoingPacket? {
        return incomingPlayerPacketFactory.firstOrNull { it.cmdId == packet.cmdId }
            ?.run {
                handle(packet)
            } ?: incomingSessionPacketFactories.firstOrNull { it.cmdId == packet.cmdId }
            ?.run {
                with(networkHandler) {
                    handle(packet)
                }
            }
    }
}

internal object PingReqFactory : IncomingPreLoginPacketFactory
<PingReq, PingRspPacket>(
    PacketId.PING_REQ
) {

    override val parser: Parser<PingReq> = PingReq.parser()

    override suspend fun NetworkHandler.handlePacket(packet: PingReq): PingRspPacket {
        updatePingTime(packet.clientTime)
        return PingRspPacket(packet)
    }
}

internal object GetPlayerTokenReqFactory : IncomingPreLoginPacketFactory
<GetPlayerTokenReq, GetPlayerTokenRspPacket>(
    PacketId.GET_PLAYER_TOKEN_REQ
) {

    override val parser: Parser<GetPlayerTokenReq> = GetPlayerTokenReq.parser()

    override suspend fun NetworkHandler.handlePacket(packet: GetPlayerTokenReq): GetPlayerTokenRspPacket {
        val uid = packet.accountUid.toUInt()
        val account = newSuspendedTransaction {
            Account.findById(uid)
        } ?: return GetPlayerTokenRspPacket.Error(Retcode.RETCODE_RET_ACCOUNT_NOT_EXIST, "user.notfound")
        if (packet.accountToken != account.getComboToken()) return GetPlayerTokenRspPacket.Error(
            Retcode.RETCODE_RET_ACCOUNT_NOT_EXIST,
            "auth.error.token"
        )
        val state = networkStateController.getStateInstance()
        if (state !is NetworkHandler.Login) return GetPlayerTokenRspPacket.Error(
            Retcode.RETCODE_RET_SVR_ERROR,
            "server.error"
        )

        val seed = randomULong()

        state.updateKeyAndBindPlayer(account, seed)

        return GetPlayerTokenRspPacket.Succ(
            packet, seed, getHost()
        )
    }
}

internal object PlayerLoginReqFactory : IncomingPacketFactoryWithResponse
<PlayerLoginReq, PlayerLoginRspPacket>(
    PacketId.PLAYER_LOGIN_REQ
) {

    override val parser: Parser<PlayerLoginReq> = PlayerLoginReq.parser()

    override suspend fun Player.handlePacket(packet: PlayerLoginReq): PlayerLoginRspPacket {

        return currentRegionRsp
            ?.let { PlayerLoginRspPacket.Succ(it) }
            ?: PlayerLoginRspPacket.Fail(Retcode.RETCODE_RET_SVR_ERROR)
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
