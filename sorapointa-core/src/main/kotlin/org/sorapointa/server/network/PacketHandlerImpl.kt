package org.sorapointa.server.network

import com.squareup.wire.ProtoAdapter
import io.ktor.util.*
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import org.sorapointa.Sorapointa
import org.sorapointa.SorapointaConfig
import org.sorapointa.crypto.clientRsaPrivateKey
import org.sorapointa.crypto.signKey
import org.sorapointa.dispatch.data.Account
import org.sorapointa.dispatch.plugins.currentRegionRsp
import org.sorapointa.game.Player
import org.sorapointa.game.data.PlayerDataImpl
import org.sorapointa.game.impl
import org.sorapointa.proto.*
import org.sorapointa.utils.randomULong
import org.sorapointa.utils.toByteArray
import org.sorapointa.utils.toULong
import org.sorapointa.utils.xor
import java.nio.ByteOrder

private val logger = KotlinLogging.logger {}

internal object PingReqHandler : IncomingPreLoginPacketHandler
<PingReq, PingRspPacket, NetworkHandlerStateInterface>(
    PacketId.PING_REQ
) {

    override val adapter: ProtoAdapter<PingReq> = PingReq.ADAPTER

    override suspend fun NetworkHandlerStateInterface.handlePacket(
        packet: PingReq
    ): PingRspPacket {
        networkHandler.updatePingTime(packet.client_time)
        return PingRspPacket(packet)
    }
}

internal object PlayerSetPauseReqHandler : IncomingPreLoginPacketHandler
<PlayerSetPauseReq, PlayerSetPauseRspPacket, NetworkHandlerStateInterface>(
    PacketId.PLAYER_SET_PAUSE_REQ
) {
    override val adapter: ProtoAdapter<PlayerSetPauseReq> = PlayerSetPauseReq.ADAPTER

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
    override val adapter: ProtoAdapter<GetPlayerTokenReq> = GetPlayerTokenReq.ADAPTER

    override suspend fun NetworkHandler.WaitToken.handlePacket(
        packet: GetPlayerTokenReq
    ): GetPlayerTokenRspPacket {
        val uid = packet.account_uid.toInt()
        val account = Account.findById(uid) ?: return GetPlayerTokenRspPacket.Error(
            Retcode.RET_ACCOUNT_NOT_EXIST, "user.notfound"
        )

        if (packet.account_token != account.getComboTokenWithCheck()) return GetPlayerTokenRspPacket.Error(
            Retcode.RET_TOKEN_ERROR, "auth.error.token"
        )
        Sorapointa.findPlayerById(uid)?.let {
            // TODO: replace it with graceful disconnect
            logger.info { "$it has been kicked out due to duplicated login" }
            it.impl().close()
        }

        // Key exchange flow, see: https://sdl.moe/post/magic-sniffer/

        val clientSeed = signKey?.decrypt(packet.client_rand_key.decodeBase64Bytes())
            ?: error("Sign private key is null or not valid")

        val serverSeed = randomULong()
        val serverSeedByte = serverSeed.toByteArray(false)

        val encryptedServerSeed = clientRsaPrivateKey?.encrypt(serverSeedByte)
            ?: error("Client public key is null or not valid")
        val sign = signKey?.sign(serverSeedByte)
            ?: error("Sign private key is null or not valid")

        val newSessionSeed = (clientSeed xor serverSeedByte).toULong(ByteOrder.BIG_ENDIAN)

        updateKeyAndBindPlayer(account, newSessionSeed)

        return GetPlayerTokenRspPacket.Successful(
            packet, networkHandler.host, encryptedServerSeed.encodeBase64(), sign.encodeBase64()
        )
    }
}

internal object PlayerLoginReqHandler : IncomingPreLoginPacketHandler
<PlayerLoginReq, PlayerLoginRspPacket, NetworkHandler.Login>(
    PacketId.PLAYER_LOGIN_REQ
) {
    override val adapter: ProtoAdapter<PlayerLoginReq> = PlayerLoginReq.ADAPTER

    override suspend fun NetworkHandler.Login.handlePacket(
        packet: PlayerLoginReq
    ): PlayerLoginRspPacket {
        return runCatching {
            withTimeout(1000) {
                val curRegion = if (SorapointaConfig.data.useCurrentRegionForLoginRsp) {
                    currentRegionRsp.await()
                } else QueryCurrRegionHttpRsp()
                if (playerData != null) {
                    setToOK(createPlayer(playerData))
                } else {
                    // Still to be `Login` state
                    networkHandler.sendPacketAsync(DoSetPlayerBornDataNotifyPacket())
                }
                curRegion
            }
        }.getOrNull()?.let {
            PlayerLoginRspPacket.Succ(it)
        } ?: PlayerLoginRspPacket.Fail(Retcode.RET_SVR_ERROR)
    }
}

internal object SetPlayerBornDataReqHandler : IncomingPreLoginPacketHandler
<SetPlayerBornDataReq, SetPlayerBornDataRspPacket, NetworkHandler.Login>(
    PacketId.SET_PLAYER_BORN_DATA_REQ
) {

    override val adapter: ProtoAdapter<SetPlayerBornDataReq> = SetPlayerBornDataReq.ADAPTER

    override suspend fun NetworkHandler.Login.handlePacket(
        packet: SetPlayerBornDataReq
    ): SetPlayerBornDataRspPacket {

        val playerData = PlayerDataImpl.create(account.id.value, packet.nick_name, packet.avatar_id)
        val player = createPlayer(playerData).apply {
            initNewPlayerAvatar(packet.avatar_id)
        }

        setToOK(player)

        return SetPlayerBornDataRspPacket()
    }
}

internal object GetPlayerSocialDetailReqHandler : IncomingPacketHandlerWithResponse
<GetPlayerSocialDetailReq, GetPlayerSocialDetailRspPacket>(
    PacketId.GET_PLAYER_SOCIAL_DETAIL_REQ
) {

    override val adapter: ProtoAdapter<GetPlayerSocialDetailReq> = GetPlayerSocialDetailReq.ADAPTER

    override suspend fun Player.handlePacket(
        packet: GetPlayerSocialDetailReq
    ): GetPlayerSocialDetailRspPacket {

        return GetPlayerSocialDetailRspPacket(this@handlePacket, packet.uid)
    }
}

internal object EnterSceneReadyReqHandler : IncomingPacketHandlerWithResponse
<EnterSceneReadyReq, EnterSceneReadyRspPacket>(
    PacketId.ENTER_SCENE_READY_REQ
) {

    override val adapter: ProtoAdapter<EnterSceneReadyReq> = EnterSceneReadyReq.ADAPTER

    override suspend fun Player.handlePacket(
        packet: EnterSceneReadyReq
    ): EnterSceneReadyRspPacket {
        return if (packet.enter_scene_token == enterSceneToken) {
            impl().sendPacketAsync(EnterScenePeerNotifyPacket(this)) // doesn't have packetHeader (metadata)
            EnterSceneReadyRspPacket.Succ(this) // has packetHeader
        } else EnterSceneReadyRspPacket.Fail(this, Retcode.RET_ENTER_SCENE_TOKEN_INVALID)
    }
}

internal object SceneInitFinishReqHandler : IncomingPacketHandlerWithResponse
<SceneInitFinishReq, SceneInitFinishRspPacket>(
    PacketId.ENTER_SCENE_READY_REQ
) {

    override val adapter: ProtoAdapter<SceneInitFinishReq> = SceneInitFinishReq.ADAPTER

    override suspend fun Player.handlePacket(
        packet: SceneInitFinishReq
    ): SceneInitFinishRspPacket {
        return if (packet.enter_scene_token == enterSceneToken) {
            // TODO: divide those into separate modules

            SceneInitFinishRspPacket.Succ(this)
        } else SceneInitFinishRspPacket.Fail(this, Retcode.RET_ENTER_SCENE_TOKEN_INVALID)
    }
}

internal object UnionCmdNotifyHandler : IncomingPacketHandlerWithoutResponse<UnionCmdNotify>(
    PacketId.UNION_CMD_NOTIFY
) {

    override val adapter: ProtoAdapter<UnionCmdNotify> = UnionCmdNotify.ADAPTER

    override suspend fun Player.handlePacket(packet: UnionCmdNotify) {
        logger.debug { "Expanding UnionCmdNotify packets..." }
        packet.cmd_list.forEach {
            val soraPacket = SoraPacket(it.message_id.toUShort(), PacketHead(), it.body.toByteArray())
            impl().forwardHandlePacket(soraPacket)
        }
    }
}
