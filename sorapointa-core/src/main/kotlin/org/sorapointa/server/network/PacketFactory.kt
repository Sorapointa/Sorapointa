package org.sorapointa.server.network

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.sorapointa.dispatch.data.Account
import org.sorapointa.dispatch.plugins.currentRegionRsp
import org.sorapointa.event.broadcastEvent
import org.sorapointa.events.HandleIncomingPacketEvent
import org.sorapointa.game.Player
import org.sorapointa.proto.*
import org.sorapointa.proto.GetPlayerTokenReqOuterClass.GetPlayerTokenReq
import org.sorapointa.proto.PingReqOuterClass.PingReq
import org.sorapointa.proto.PlayerLoginReqOuterClass.PlayerLoginReq
import org.sorapointa.proto.RetcodeOuterClass.Retcode
import org.sorapointa.utils.randomULong

private val logger = KotlinLogging.logger {}

/**
 * Server passively receives a packet (requst),
 * client will require a response, copy metadata in this case
 */
internal abstract class IncomingPacketFactory
<TPacketReq : GeneratedMessageV3>(
    val cmdId: UShort
) {

    protected abstract val parser: Parser<TPacketReq>

    abstract suspend fun Player.handle(soraPacket: SoraPacket): OutgoingPacket?

    fun parsing(data: ByteArray): TPacketReq = parser.parseFrom(data)
}

internal abstract class IncomingPacketFactoryWithoutResponse
<TPacketReq : GeneratedMessageV3>(
    cmdId: UShort
) : IncomingPacketFactory<TPacketReq>(cmdId) {

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
) : IncomingPacketFactory<TPacketReq>(cmdId) {

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

internal object IncomingPacketFactories {

    private val incomingPacketFactories = listOf<IncomingPacketFactory<*>>(
        GetPlayerTokenReqFactory,
        PingReqFactory
    )

    suspend fun Player.handlePacket(packet: SoraPacket): OutgoingPacket? {
        logger.debug { "Recv: ${findCommonNameFromCmdId(packet.cmdId)} Id: ${packet.cmdId}" }
        return incomingPacketFactories.firstOrNull { it.cmdId == packet.cmdId }
            ?.run {
                handle(packet)
            }
    }
}

internal object PingReqFactory : IncomingPacketFactoryWithResponse
<PingReq, PingRspPacket>(
    PacketId.PING_REQ
) {

    override val parser: Parser<PingReq> = PingReq.parser()

    override suspend fun Player.handlePacket(packet: PingReq): PingRspPacket {
        // TODO: Update Player Time
        return PingRspPacket(packet)
    }
}

internal object GetPlayerTokenReqFactory : IncomingPacketFactoryWithResponse
<GetPlayerTokenReq, GetPlayerTokenRspPacket>(
    PacketId.GET_PLAYER_TOKEN_REQ
) {

    override val parser: Parser<GetPlayerTokenReq> = GetPlayerTokenReq.parser()

    override suspend fun Player.handlePacket(packet: GetPlayerTokenReq): GetPlayerTokenRspPacket {
        val account = newSuspendedTransaction {
            Account.findById(packet.accountUid.toUInt())
        } ?: return GetPlayerTokenRspPacket.Error(Retcode.RETCODE_RET_ACCOUNT_NOT_EXIST, "user.notfound")
        if (packet.accountToken != account.getComboToken()) return GetPlayerTokenRspPacket.Error(
            Retcode.RETCODE_RET_ACCOUNT_NOT_EXIST,
            "auth.failed"
        )

        this.account = account

        val seed = randomULong()
        networkHandler.updateKeyWithSeed(seed)

        return GetPlayerTokenRspPacket.Succ(
            packet, seed, networkHandler.getHost()
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
