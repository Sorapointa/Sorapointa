package org.sorapointa.server.network

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.sorapointa.dispatch.data.Account
import org.sorapointa.game.Player
import org.sorapointa.proto.PacketId
import org.sorapointa.proto.GetPlayerTokenReqOuterClass.GetPlayerTokenReq
import org.sorapointa.proto.RetcodeOuterClass.Retcode
import org.sorapointa.proto.SoraPacket
import org.sorapointa.utils.randomULong

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
}

internal abstract class IncomingPacketFactoryWithoutResponse
<TPacketReq : GeneratedMessageV3>(
    cmdId: UShort
) : IncomingPacketFactory<TPacketReq>(cmdId) {

    protected abstract suspend fun Player.handlePacket(packet: TPacketReq)

    override suspend fun Player.handle(soraPacket: SoraPacket): OutgoingPacket? {
        handlePacket(parser.parseFrom(soraPacket.data))
        return null // No response for this type of packet
    }
}

internal abstract class IncomingPacketFactoryWithResponse
<TPacketReq : GeneratedMessageV3, TPacketRsp : OutgoingPacket>(
    cmdId: UShort
) : IncomingPacketFactory<TPacketReq>(cmdId) {

    protected abstract suspend fun Player.handlePacket(packet: TPacketReq): TPacketRsp

    override suspend fun Player.handle(soraPacket: SoraPacket): OutgoingPacket? {
        val rsp = handlePacket(parser.parseFrom(soraPacket.data))
        rsp.metadata = soraPacket.metadata
        return rsp
    }
}

internal object IncomingPacketFactories {

    private val incomingPacketFactories = listOf<IncomingPacketFactory<*>>(
        GetPlayerTokenReqFactory
    )

    suspend fun Player.handlePacket(packet: SoraPacket): OutgoingPacket? =
        incomingPacketFactories.firstOrNull { it.cmdId == packet.cmdId }
            ?.run {
                handle(packet)
            }
}

internal object GetPlayerTokenReqFactory : IncomingPacketFactoryWithResponse
<GetPlayerTokenReq, GetPlayerTokenRspPacket>(
    PacketId.GET_PLAYER_TOKEN_REQ
) {

    override val parser: Parser<GetPlayerTokenReq> = GetPlayerTokenReq.parser()

    override suspend fun Player.handlePacket(packet: GetPlayerTokenReq): GetPlayerTokenRspPacket {
        val account = newSuspendedTransaction {
            Account.findById(packet.uid.toUInt())
        } ?: return GetPlayerTokenRspPacket.Error(Retcode.RETCODE_RET_ACCOUNT_NOT_EXIST, "user.notfound")
        if (packet.accountToken != account.getComboToken()) return GetPlayerTokenRspPacket.Error(
            Retcode.RETCODE_RET_ACCOUNT_NOT_EXIST,
            "auth.failed"
        )

        val seed = randomULong()
        networkHandler.updateKeyWithSeed(seed)

        return GetPlayerTokenRspPacket.Succ(
            packet, seed, networkHandler.getHost()
        )
    }
}

// internal object PlayerLoginReqFactory: IncomingPacketFactoryWithResponse
// <PlayerLoginReq, PlayerLoginRsp>

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
