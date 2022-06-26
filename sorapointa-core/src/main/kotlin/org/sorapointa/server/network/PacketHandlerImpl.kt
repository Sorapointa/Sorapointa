package org.sorapointa.server.network

import com.google.protobuf.Parser
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.sorapointa.Sorapointa
import org.sorapointa.dispatch.data.Account
import org.sorapointa.dispatch.plugins.currentRegionRsp
import org.sorapointa.game.Player
import org.sorapointa.game.data.PlayerDataImpl
import org.sorapointa.game.impl
import org.sorapointa.proto.EnterSceneReadyReqOuterClass.EnterSceneReadyReq
import org.sorapointa.proto.GetPlayerSocialDetailReqOuterClass.GetPlayerSocialDetailReq
import org.sorapointa.proto.GetPlayerTokenReqOuterClass.GetPlayerTokenReq
import org.sorapointa.proto.PacketId
import org.sorapointa.proto.PingReqOuterClass.PingReq
import org.sorapointa.proto.PlayerLoginReqOuterClass.PlayerLoginReq
import org.sorapointa.proto.PlayerSetPauseReqOuterClass.PlayerSetPauseReq
import org.sorapointa.proto.RetcodeOuterClass.Retcode
import org.sorapointa.proto.SceneInitFinishReqOuterClass.SceneInitFinishReq
import org.sorapointa.proto.SetPlayerBornDataReqOuterClass.SetPlayerBornDataReq
import org.sorapointa.proto.SoraPacket
import org.sorapointa.proto.UnionCmdNotifyOuterClass.UnionCmdNotify
import org.sorapointa.proto.packetHead
import org.sorapointa.utils.randomULong

private val logger = KotlinLogging.logger {}

internal object PingReqHandler : IncomingPreLoginPacketHandler
<PingReq, PingRspPacket, NetworkHandlerStateInterface>(
    PacketId.PING_REQ
) {

    override val parser: Parser<PingReq> = PingReq.parser()

    override suspend fun NetworkHandlerStateInterface.handlePacket(
        packet: PingReq
    ): PingRspPacket {
        networkHandler.updatePingTime(packet.clientTime)
        return PingRspPacket(packet)
    }
}

internal object PlayerSetPauseReqHandler : IncomingPreLoginPacketHandler
<PlayerSetPauseReq, PlayerSetPauseRspPacket, NetworkHandlerStateInterface>(
    PacketId.PLAYER_SET_PAUSE_REQ
) {

    override val parser: Parser<PlayerSetPauseReq> = PlayerSetPauseReq.parser()

    override suspend fun NetworkHandlerStateInterface.handlePacket(
        packet: PlayerSetPauseReq
    ): PlayerSetPauseRspPacket {

        return PlayerSetPauseRspPacket()
    }
}

internal object GetPlayerTokenReqHandler : IncomingPreLoginPacketHandler
<GetPlayerTokenReq, GetPlayerTokenRspPacket, NetworkHandler.WaitToken>(
    PacketId.GET_PLAYER_TOKEN_REQ
) {

    override val parser: Parser<GetPlayerTokenReq> =
        GetPlayerTokenReq.parser()

    override suspend fun NetworkHandler.WaitToken.handlePacket(
        packet: GetPlayerTokenReq
    ): GetPlayerTokenRspPacket {
        val uid = packet.accountUid.toInt()
        val account = newSuspendedTransaction {
            Account.findById(uid)
        } ?: return GetPlayerTokenRspPacket.Error(
            Retcode.RETCODE_RET_ACCOUNT_NOT_EXIST, "user.notfound"
        )
        if (packet.accountToken != account.getComboTokenWithCheck()) return GetPlayerTokenRspPacket.Error(
            Retcode.RETCODE_RET_ACCOUNT_NOT_EXIST, "auth.error.token"
        )
        Sorapointa.findPlayerById(uid)?.let {
            // TODO: replace it with graceful disconnect
            logger.info { "$it has been kicked out due to duplicated login" }
            it.impl().close()
        }

        val seed = randomULong()

        updateKeyAndBindPlayer(account, seed)

        return GetPlayerTokenRspPacket.Successful(
            packet, seed, networkHandler.getHost()
        )
    }
}

internal object PlayerLoginReqHandler : IncomingPreLoginPacketHandler
<PlayerLoginReq, PlayerLoginRspPacket, NetworkHandler.Login>(
    PacketId.PLAYER_LOGIN_REQ
) {

    override val parser: Parser<PlayerLoginReq> =
        PlayerLoginReq.parser()

    override suspend fun NetworkHandler.Login.handlePacket(
        packet: PlayerLoginReq
    ): PlayerLoginRspPacket {

        newSuspendedTransaction {
            if (playerData != null) {
                setToOK(createPlayer(playerData))
            } else {
                // Still to be `Login` state
                networkHandler.sendPacket(DoSetPlayerBornDataNotifyPacket())
            }
        }

        return currentRegionRsp
            ?.let { PlayerLoginRspPacket.Succ(it) }
            ?: PlayerLoginRspPacket.Fail(Retcode.RETCODE_RET_SVR_ERROR)
    }
}

internal object SetPlayerBornDataReqHandler : IncomingPreLoginPacketHandler
<SetPlayerBornDataReq, SetPlayerBornDataRspPacket, NetworkHandler.Login>(
    PacketId.SET_PLAYER_BORN_DATA_REQ
) {

    override val parser: Parser<SetPlayerBornDataReq> = SetPlayerBornDataReq.parser()

    override suspend fun NetworkHandler.Login.handlePacket(
        packet: SetPlayerBornDataReq
    ): SetPlayerBornDataRspPacket {

        val player = newSuspendedTransaction {
            val playerData = PlayerDataImpl.create(account.id.value, packet.nickName, packet.avatarId)
            createPlayer(playerData).apply {
                initNewPlayerAvatar(packet.avatarId)
            }
        }

        newSuspendedTransaction {
            setToOK(player)
        }

        return SetPlayerBornDataRspPacket()
    }
}

internal object GetPlayerSocialDetailReqHandler : IncomingPacketHandlerWithResponse
<GetPlayerSocialDetailReq, GetPlayerSocialDetailRspPacket>(
    PacketId.GET_PLAYER_SOCIAL_DETAIL_REQ
) {

    override val parser: Parser<GetPlayerSocialDetailReq> = GetPlayerSocialDetailReq.parser()

    override suspend fun Player.handlePacket(
        packet: GetPlayerSocialDetailReq
    ): GetPlayerSocialDetailRspPacket {

        return newSuspendedTransaction {
            GetPlayerSocialDetailRspPacket(this@handlePacket, packet.uid)
        }

    }

}

internal object EnterSceneReadyReqHandler : IncomingPacketHandlerWithResponse
<EnterSceneReadyReq, EnterSceneReadyRspPacket>(
    PacketId.ENTER_SCENE_READY_REQ
) {

    override val parser: Parser<EnterSceneReadyReq> = EnterSceneReadyReq.parser()

    override suspend fun Player.handlePacket(
        packet: EnterSceneReadyReq
    ): EnterSceneReadyRspPacket {
        return if (packet.enterSceneToken == enterSceneToken) {
            impl().sendPacket(EnterScenePeerNotifyPacket(this)) // doesn't have packetHeader (metadata)
            EnterSceneReadyRspPacket.Succ(this) // has packetHeader
        } else EnterSceneReadyRspPacket.Fail(this, Retcode.RETCODE_RET_ENTER_SCENE_TOKEN_INVALID)
    }

}

internal object SceneInitFinishReqHandler : IncomingPacketHandlerWithResponse
<SceneInitFinishReq, SceneInitFinishRspPacket>(
    PacketId.ENTER_SCENE_READY_REQ
) {

    override val parser: Parser<SceneInitFinishReq> = SceneInitFinishReq.parser()

    override suspend fun Player.handlePacket(
        packet: SceneInitFinishReq
    ): SceneInitFinishRspPacket {
        return if (packet.enterSceneToken == enterSceneToken) {
            // TODO: divide those into separate modules

            SceneInitFinishRspPacket.Succ(this)
        } else SceneInitFinishRspPacket.Fail(this, Retcode.RETCODE_RET_ENTER_SCENE_TOKEN_INVALID)
    }

}

internal object UnionCmdNotifyHandler: IncomingPacketHandlerWithoutResponse<UnionCmdNotify>(
    PacketId.UNION_CMD_NOTIFY
) {

    override val parser: Parser<UnionCmdNotify> = UnionCmdNotify.parser()

    override suspend fun Player.handlePacket(packet: UnionCmdNotify) {
        logger.debug { "Expanding UnionCmdNotify packets..." }
        packet.cmdListList.forEach {
            val soraPacket = SoraPacket(it.messageId.toUShort(), packetHead {  }, it.body.toByteArray())
            impl().forwardHandlePacket(soraPacket)
        }
    }

}




