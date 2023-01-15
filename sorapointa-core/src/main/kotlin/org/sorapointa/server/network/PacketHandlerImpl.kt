package org.sorapointa.server.network

import com.squareup.wire.ProtoAdapter
import io.ktor.util.*
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging
import org.sorapointa.Sorapointa
import org.sorapointa.SorapointaConfig
import org.sorapointa.crypto.clientRsaPrivateKey
import org.sorapointa.crypto.signKey
import org.sorapointa.dispatch.data.Account
import org.sorapointa.dispatch.plugins.readCurrRegionCacheOrRequest
import org.sorapointa.event.broadcast
import org.sorapointa.events.PlayerFirstCreateEvent
import org.sorapointa.game.data.PlayerData
import org.sorapointa.proto.*
import org.sorapointa.utils.randomULong
import org.sorapointa.utils.toByteArray
import org.sorapointa.utils.toULong
import org.sorapointa.utils.xor
import java.nio.ByteOrder

private val logger = KotlinLogging.logger {}

internal object PingReqHandler : PreLoginPacketHandler<PingReq, NetworkHandlerStateI>(
    PacketId.PING_REQ,
) {

    override val adapter: ProtoAdapter<PingReq> = PingReq.ADAPTER

    override suspend fun PacketHandlerContext<NetworkHandlerStateI>.handlePacket(packet: PingReq) {
        state.networkHandler.updatePingTime(packet.client_time)
        sendPacket(PingRspPacket(packet))
    }
}

internal object PlayerSetPauseReqHandler : PreLoginPacketHandler<PlayerSetPauseReq, NetworkHandlerStateI>(
    PacketId.PLAYER_SET_PAUSE_REQ,
) {
    override val adapter: ProtoAdapter<PlayerSetPauseReq> = PlayerSetPauseReq.ADAPTER

    override suspend fun PacketHandlerContext<NetworkHandlerStateI>.handlePacket(packet: PlayerSetPauseReq) {
        sendPacket(PlayerSetPauseRspPacket())
    }
}

internal object GetPlayerTokenReqHandler : PreLoginPacketHandler<GetPlayerTokenReq, NetworkHandler.WaitToken>(
    PacketId.GET_PLAYER_TOKEN_REQ,
) {
    override val adapter: ProtoAdapter<GetPlayerTokenReq> = GetPlayerTokenReq.ADAPTER

    override suspend fun PacketHandlerContext<NetworkHandler.WaitToken>.handlePacket(
        packet: GetPlayerTokenReq,
    ) {
        val uid = packet.account_uid.toInt()
        val account = Account.findById(uid)
        if (account == null) {
            sendPacket(
                GetPlayerTokenRspPacket.Err(
                    Retcode.RET_ACCOUNT_NOT_EXIST,
                    "user.notfound",
                ),
            )
            return
        }
        if (packet.account_token != account.getComboTokenWithCheck()) {
            sendPacket(
                GetPlayerTokenRspPacket.Err(
                    Retcode.RET_TOKEN_ERROR,
                    "auth.error.token",
                ),
            )
            return
        }

        Sorapointa.findPlayerByIdOrNull(uid)?.let {
            // TODO: replace it with graceful disconnect
            logger.info { "$it has been kicked out due to duplicated login" }
            it.close()
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

        state.updateKeyAndBindPlayer(account, newSessionSeed)

        sendPacketSync(
            GetPlayerTokenRspPacket.Succ(
                packet,
                state.networkHandler.host,
                encryptedServerSeed.encodeBase64(),
                sign.encodeBase64(),
            ),
        )
    }
}

internal object PlayerLoginReqHandler : PreLoginPacketHandler<PlayerLoginReq, NetworkHandler.Login>(
    PacketId.PLAYER_LOGIN_REQ,
) {
    override val adapter: ProtoAdapter<PlayerLoginReq> = PlayerLoginReq.ADAPTER

    override suspend fun PacketHandlerContext<NetworkHandler.Login>.handlePacket(
        packet: PlayerLoginReq,
    ) {
        if (state.playerData == null) {
            // Still to be `Login` state
            if (SorapointaConfig.data.debugSetting.skipBornCutscene) {
                logger.debug { "Skipped born cutscene and auto choose nickname and avatar" }
                SetPlayerBornDataReqHandler.createNewPlayer(
                    ctx = this,
                    nickname = "SpTest",
                    pickAvatarId = 10000007, // PlayerGirl
                )
            } else {
                sendPacket(DoSetPlayerBornDataNotifyPacket())
            }
        } else {
            state.setToOK(state.createPlayer(state.playerData), metadata)
        }
        withTimeoutOrNull(5000) {
            if (SorapointaConfig.data.useCurrentRegionForLoginRsp) {
                readCurrRegionCacheOrRequest()
            } else {
                QueryCurrRegionHttpRsp()
            }
        }?.let {
            PlayerLoginRspPacket.Succ(it)
        } ?: PlayerLoginRspPacket.Err(Retcode.RET_SVR_ERROR)
    }
}

internal object SetPlayerBornDataReqHandler : PreLoginPacketHandler<SetPlayerBornDataReq, NetworkHandler.Login>(
    PacketId.SET_PLAYER_BORN_DATA_REQ,
) {

    override val adapter: ProtoAdapter<SetPlayerBornDataReq> = SetPlayerBornDataReq.ADAPTER

    suspend fun createNewPlayer(
        ctx: PacketHandlerContext<NetworkHandler.Login>,
        nickname: String,
        pickAvatarId: Int,
    ) {
        val playerData = PlayerData.create(ctx.state.account.id.value, nickname, pickAvatarId)
        val player = ctx.state.createPlayer(playerData)
        PlayerFirstCreateEvent(player, ctx.metadata, pickAvatarId).broadcast()
        player.saveData()

        ctx.state.setToOK(player, ctx.metadata)
    }

    override suspend fun PacketHandlerContext<NetworkHandler.Login>.handlePacket(
        packet: SetPlayerBornDataReq,
    ) {
        createNewPlayer(this, packet.nick_name, packet.avatar_id)
        sendPacket(SetPlayerBornDataRspPacket())
    }
}

internal object GetPlayerSocialDetailReqHandler : PlayerPacketHandler<GetPlayerSocialDetailReq>(
    PacketId.GET_PLAYER_SOCIAL_DETAIL_REQ,
) {

    override val adapter: ProtoAdapter<GetPlayerSocialDetailReq> = GetPlayerSocialDetailReq.ADAPTER

    override suspend fun PlayerPacketHandlerContext.handlePacket(
        packet: GetPlayerSocialDetailReq,
    ) {
        val targetUid = packet.uid
        val targetPlayer = Sorapointa.findPlayerByIdOrNull(targetUid)

        if (targetPlayer == null) {
            val targetPlayerData = PlayerData.findById(targetUid)?.getPlayerDataBin()
            if (targetPlayerData != null) {
                sendPacket(GetPlayerSocialDetailRspPacket.SuccOffline(player, targetUid, targetPlayerData))
            } else {
                sendPacket(GetPlayerSocialDetailRspPacket.Err(player, Retcode.RET_PLAYER_NOT_EXIST))
            }
            return
        }

        sendPacket(GetPlayerSocialDetailRspPacket.SuccOnline(player, targetPlayer))
    }
}

internal object EnterSceneReadyReqHandler : PlayerPacketHandler<EnterSceneReadyReq>(
    PacketId.ENTER_SCENE_READY_REQ,
) {

    override val adapter: ProtoAdapter<EnterSceneReadyReq> = EnterSceneReadyReq.ADAPTER

    override suspend fun PlayerPacketHandlerContext.handlePacket(
        packet: EnterSceneReadyReq,
    ) {
        if (packet.enter_scene_token == player.enterSceneToken) {
            sendPacketSync(EnterScenePeerNotifyPacket(player))
            sendPacketSync(EnterSceneReadyRspPacket.Succ(player))
        } else {
            sendPacket(EnterSceneReadyRspPacket.Fail(player, Retcode.RET_ENTER_SCENE_TOKEN_INVALID))
        }
    }
}

internal object EnterSceneDoneReqHandler : PlayerPacketHandler<EnterSceneDoneReq>(
    PacketId.ENTER_SCENE_DONE_REQ,
) {

    override val adapter: ProtoAdapter<EnterSceneDoneReq> = EnterSceneDoneReq.ADAPTER

    override suspend fun PlayerPacketHandlerContext.handlePacket(
        packet: EnterSceneDoneReq,
    ) {
        if (packet.enter_scene_token == player.enterSceneToken) {
            sendPacketSync(SceneEntityAppearNotifyPacket(player))
            sendPacketSync(EnterSceneDoneRspPacket.Succ(player))
        } else {
            sendPacket(EnterSceneDoneRspPacket.Fail(player, Retcode.RET_ENTER_SCENE_TOKEN_INVALID))
        }
    }
}

internal object SceneInitFinishReqHandler : PlayerPacketHandler<SceneInitFinishReq>(
    PacketId.SCENE_INIT_FINISH_REQ,
) {

    override val adapter: ProtoAdapter<SceneInitFinishReq> = SceneInitFinishReq.ADAPTER

    override suspend fun PlayerPacketHandlerContext.handlePacket(
        packet: SceneInitFinishReq,
    ) {
        if (packet.enter_scene_token == player.enterSceneToken) {
            // TODO: divide those into separate modules
            sendPacketSync(SceneTeamUpdateNotifyPacket(player))
            sendPacketSync(PlayerEnterSceneInfoNotifyPacket(player))
            sendPacketSync(SceneInitFinishRspPacket.Succ(player))
        } else {
            sendPacket(SceneInitFinishRspPacket.Fail(player, Retcode.RET_ENTER_SCENE_TOKEN_INVALID))
        }
    }
}

internal object PostEnterSceneReqHandler : PlayerPacketHandler<PostEnterSceneReq>(
    PacketId.POST_ENTER_SCENE_REQ,
) {

    override val adapter: ProtoAdapter<PostEnterSceneReq> = PostEnterSceneReq.ADAPTER

    override suspend fun PlayerPacketHandlerContext.handlePacket(
        packet: PostEnterSceneReq,
    ) {
        if (packet.enter_scene_token == player.enterSceneToken) {
            sendPacket(PostEnterSceneRspPacket.Succ(player)) // has packetHeader
        } else {
            sendPacket(PostEnterSceneRspPacket.Fail(player, Retcode.RET_ENTER_SCENE_TOKEN_INVALID))
        }
    }
}

internal object UnionCmdNotifyHandler : PreLoginPacketHandler<UnionCmdNotify, NetworkHandlerStateI>(
    PacketId.UNION_CMD_NOTIFY,
) {

    override val adapter: ProtoAdapter<UnionCmdNotify> = UnionCmdNotify.ADAPTER

    override suspend fun PacketHandlerContext<NetworkHandlerStateI>.handlePacket(packet: UnionCmdNotify) {
        packet.cmd_list.forEach {
            val soraPacket = SoraPacket(it.message_id.toUShort(), metadata, it.body.toByteArray())
            state.handlePacket(soraPacket)
        }
    }
}
