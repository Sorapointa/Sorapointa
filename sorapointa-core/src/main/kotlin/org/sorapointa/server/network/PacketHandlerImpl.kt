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
import org.sorapointa.dispatch.plugins.readCurrRegionCacheOrRequest
import org.sorapointa.event.broadcast
import org.sorapointa.events.PlayerFirstCreateEvent
import org.sorapointa.game.Player
import org.sorapointa.game.data.PlayerData
import org.sorapointa.game.impl
import org.sorapointa.proto.*
import org.sorapointa.utils.randomULong
import org.sorapointa.utils.toByteArray
import org.sorapointa.utils.toULong
import org.sorapointa.utils.xor
import java.nio.ByteOrder

private val logger = KotlinLogging.logger {}

internal object PingReqHandler : IncomingPreLoginPacketHandler<PingReq, PingRspPacket, NetworkHandlerStateInterface>(
    PacketId.PING_REQ,
) {

    override val adapter: ProtoAdapter<PingReq> = PingReq.ADAPTER

    override suspend fun NetworkHandlerStateInterface.handlePacket(
        packet: PingReq,
    ): PingRspPacket {
        networkHandler.updatePingTime(packet.client_time)
        return PingRspPacket(packet)
    }
}

internal object PlayerSetPauseReqHandler : IncomingPreLoginPacketHandler<PlayerSetPauseReq, PlayerSetPauseRspPacket, NetworkHandlerStateInterface>(
    PacketId.PLAYER_SET_PAUSE_REQ,
) {
    override val adapter: ProtoAdapter<PlayerSetPauseReq> = PlayerSetPauseReq.ADAPTER

    override suspend fun NetworkHandlerStateInterface.handlePacket(
        packet: PlayerSetPauseReq,
    ): PlayerSetPauseRspPacket {
        return PlayerSetPauseRspPacket()
    }
}

internal object GetPlayerTokenReqHandler : IncomingPreLoginPacketHandler<GetPlayerTokenReq, GetPlayerTokenRspPacket, NetworkHandler.WaitToken>(
    PacketId.GET_PLAYER_TOKEN_REQ,
) {
    override val adapter: ProtoAdapter<GetPlayerTokenReq> = GetPlayerTokenReq.ADAPTER

    override suspend fun NetworkHandler.WaitToken.handlePacket(
        packet: GetPlayerTokenReq,
    ): GetPlayerTokenRspPacket {
        val uid = packet.account_uid.toInt()
        val account = Account.findById(uid) ?: return GetPlayerTokenRspPacket.Err(
            Retcode.RET_ACCOUNT_NOT_EXIST,
            "user.notfound",
        )

        if (packet.account_token != account.getComboTokenWithCheck()) {
            return GetPlayerTokenRspPacket.Err(
                Retcode.RET_TOKEN_ERROR,
                "auth.error.token",
            )
        }
        Sorapointa.findOrNullPlayerById(uid)?.let {
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

        return GetPlayerTokenRspPacket.Succ(
            packet,
            networkHandler.host,
            encryptedServerSeed.encodeBase64(),
            sign.encodeBase64(),
        )
    }
}

internal object PlayerLoginReqHandler : IncomingPreLoginPacketHandler<PlayerLoginReq, PlayerLoginRspPacket, NetworkHandler.Login>(
    PacketId.PLAYER_LOGIN_REQ,
) {
    override val adapter: ProtoAdapter<PlayerLoginReq> = PlayerLoginReq.ADAPTER

    override suspend fun NetworkHandler.Login.handlePacket(
        packet: PlayerLoginReq,
    ): PlayerLoginRspPacket {
        return runCatching {
            withTimeout(5000) {
                val curRegion = if (SorapointaConfig.data.useCurrentRegionForLoginRsp) {
                    readCurrRegionCacheOrRequest()
                } else {
                    QueryCurrRegionHttpRsp()
                }
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
        } ?: PlayerLoginRspPacket.Err(Retcode.RET_SVR_ERROR)
    }
}

internal object SetPlayerBornDataReqHandler : IncomingPreLoginPacketHandler<SetPlayerBornDataReq, SetPlayerBornDataRspPacket, NetworkHandler.Login>(
    PacketId.SET_PLAYER_BORN_DATA_REQ,
) {

    override val adapter: ProtoAdapter<SetPlayerBornDataReq> = SetPlayerBornDataReq.ADAPTER

    override suspend fun NetworkHandler.Login.handlePacket(
        packet: SetPlayerBornDataReq,
    ): SetPlayerBornDataRspPacket {
        val pickAvatarId = packet.avatar_id
        val playerData = PlayerData.create(account.id.value, packet.nick_name, pickAvatarId)
        val player = createPlayer(playerData)
        PlayerFirstCreateEvent(player, pickAvatarId).broadcast()
        player.impl().saveData()

        setToOK(player)

        return SetPlayerBornDataRspPacket()
    }
}

internal object GetPlayerSocialDetailReqHandler : IncomingPacketHandlerWithResponse<GetPlayerSocialDetailReq, GetPlayerSocialDetailRspPacket>(
    PacketId.GET_PLAYER_SOCIAL_DETAIL_REQ,
) {

    override val adapter: ProtoAdapter<GetPlayerSocialDetailReq> = GetPlayerSocialDetailReq.ADAPTER

    override suspend fun Player.handlePacket(
        packet: GetPlayerSocialDetailReq,
    ): GetPlayerSocialDetailRspPacket {
        val targetUid = packet.uid
        val targetPlayer = Sorapointa.findOrNullPlayerById(targetUid)

        return if (targetPlayer != null) {
            GetPlayerSocialDetailRspPacket.SuccOnline(this, targetPlayer)
        } else {
            val targetPlayerData = PlayerData.findById(targetUid)?.getPlayerDataBin()
            if (targetPlayerData != null) {
                GetPlayerSocialDetailRspPacket.SuccOffline(this, targetUid, targetPlayerData)
            } else {
                GetPlayerSocialDetailRspPacket.Err(this, Retcode.RET_PLAYER_NOT_EXIST)
            }
        }
    }
}

internal object EnterSceneReadyReqHandler : IncomingPacketHandlerWithResponse<EnterSceneReadyReq, EnterSceneReadyRspPacket>(
    PacketId.ENTER_SCENE_READY_REQ,
) {

    override val adapter: ProtoAdapter<EnterSceneReadyReq> = EnterSceneReadyReq.ADAPTER

    override suspend fun Player.handlePacket(
        packet: EnterSceneReadyReq,
    ): EnterSceneReadyRspPacket {
        return if (packet.enter_scene_token == enterSceneToken) {
            impl().sendPacket(EnterScenePeerNotifyPacket(this)) // doesn't have packetHeader (metadata)
            EnterSceneReadyRspPacket.Succ(this) // has packetHeader
        } else {
            EnterSceneReadyRspPacket.Fail(this, Retcode.RET_ENTER_SCENE_TOKEN_INVALID)
        }
    }
}

internal object EnterSceneDoneReqHandler : IncomingPacketHandlerWithResponse<EnterSceneDoneReq, EnterSceneDoneRspPacket>(
    PacketId.ENTER_SCENE_DONE_REQ,
) {

    override val adapter: ProtoAdapter<EnterSceneDoneReq> = EnterSceneDoneReq.ADAPTER

    override suspend fun Player.handlePacket(
        packet: EnterSceneDoneReq,
    ): EnterSceneDoneRspPacket {
        return if (packet.enter_scene_token == enterSceneToken) {
            impl().sendPacket(SceneEntityAppearNotifyPacket(this))
            EnterSceneDoneRspPacket.Succ(this) // has packetHeader
        } else {
            EnterSceneDoneRspPacket.Fail(this, Retcode.RET_ENTER_SCENE_TOKEN_INVALID)
        }
    }
}

internal object SceneInitFinishReqHandler : IncomingPacketHandlerWithResponse<SceneInitFinishReq, SceneInitFinishRspPacket>(
    PacketId.SCENE_INIT_FINISH_REQ,
) {

    override val adapter: ProtoAdapter<SceneInitFinishReq> = SceneInitFinishReq.ADAPTER

    override suspend fun Player.handlePacket(
        packet: SceneInitFinishReq,
    ): SceneInitFinishRspPacket {
        return if (packet.enter_scene_token == enterSceneToken) {
            // TODO: divide those into separate modules
            impl().sendPacket(SceneTeamUpdateNotifyPacket(this))
            impl().sendPacket(PlayerEnterSceneInfoNotifyPacket(this))
            SceneInitFinishRspPacket.Succ(this)
        } else {
            SceneInitFinishRspPacket.Fail(this, Retcode.RET_ENTER_SCENE_TOKEN_INVALID)
        }
    }
}

internal object PostEnterSceneReqHandler : IncomingPacketHandlerWithResponse<PostEnterSceneReq, PostEnterSceneRspPacket>(
    PacketId.POST_ENTER_SCENE_REQ,
) {

    override val adapter: ProtoAdapter<PostEnterSceneReq> = PostEnterSceneReq.ADAPTER

    override suspend fun Player.handlePacket(
        packet: PostEnterSceneReq,
    ): PostEnterSceneRspPacket {
        return if (packet.enter_scene_token == enterSceneToken) {
            PostEnterSceneRspPacket.Succ(this) // has packetHeader
        } else {
            PostEnterSceneRspPacket.Fail(this, Retcode.RET_ENTER_SCENE_TOKEN_INVALID)
        }
    }
}

internal object UnionCmdNotifyHandler : IncomingPacketHandlerWithoutResponse<UnionCmdNotify>(
    PacketId.UNION_CMD_NOTIFY,
) {

    override val adapter: ProtoAdapter<UnionCmdNotify> = UnionCmdNotify.ADAPTER

    override suspend fun Player.handlePacket(packet: UnionCmdNotify) {
        packet.cmd_list.forEach {
            val soraPacket = SoraPacket(it.message_id.toUShort(), PacketHead(), it.body.toByteArray())
            impl().forwardHandlePacket(soraPacket)
        }
    }
}
