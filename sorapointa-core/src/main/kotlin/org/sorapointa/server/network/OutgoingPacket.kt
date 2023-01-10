package org.sorapointa.server.network

import com.squareup.wire.ProtoAdapter
import io.ktor.util.*
import okio.ByteString.Companion.toByteString
import org.sorapointa.CoreBundle
import org.sorapointa.Sorapointa
import org.sorapointa.SorapointaConfig
import org.sorapointa.dataloader.common.EnterReason
import org.sorapointa.dataloader.common.OpenState
import org.sorapointa.dataloader.common.WorldType
import org.sorapointa.game.Avatar
import org.sorapointa.game.Player
import org.sorapointa.game.data.PlayerDataImpl
import org.sorapointa.game.data.PlayerFriendRelationTable
import org.sorapointa.game.impl
import org.sorapointa.game.toFlattenPropMap
import org.sorapointa.proto.*
import org.sorapointa.utils.nowMilliseconds
import org.sorapointa.utils.nowSeconds
import org.sorapointa.utils.randomByteArray

// --- Session ---

internal abstract class GetPlayerTokenRspPacket : AbstractOutgoingPacket<GetPlayerTokenRsp>(
    PacketId.GET_PLAYER_TOKEN_RSP,
) {

    override val adapter: ProtoAdapter<GetPlayerTokenRsp>
        get() = GetPlayerTokenRsp.ADAPTER

    internal class Error(
        private val retcode: Retcode,
        private val msg: String,
    ) : GetPlayerTokenRspPacket() {
        override fun buildProto(): GetPlayerTokenRsp =
            GetPlayerTokenRsp(
                msg = CoreBundle.message(this@Error.msg),
                retcode = this@Error.retcode.value,
            )
    }

    internal class Successful(
        private val tokenReq: GetPlayerTokenReq,
        private val keySeed: ULong,
        private val ip: String
    ) : GetPlayerTokenRspPacket() {
        override fun buildProto(): GetPlayerTokenRsp =
            GetPlayerTokenRsp(
                uid = tokenReq.account_uid.toInt(),
                token = tokenReq.account_token,
                account_type = tokenReq.account_type,
                account_uid = tokenReq.account_uid,
                is_proficient_player = true,
                secret_key_seed = keySeed.toLong(),
                security_cmd_buffer = randomByteArray(32).toByteString(),
                platform_type = tokenReq.platform_type,
                channel_id = tokenReq.channel_id,
                sub_channel_id = tokenReq.sub_channel_id,
                country_code = "US",
                client_version_random_key = "aeb-bc90f1631c05",
                reg_platform = 1,
                client_ip_str = ip,
            )
    }
}

private val REGISTRY_CPS = "bWlob3lv".decodeBase64String()

internal abstract class PlayerLoginRspPacket : AbstractOutgoingPacket<PlayerLoginRsp>(
    PacketId.PLAYER_LOGIN_RSP
) {
    override val adapter: ProtoAdapter<PlayerLoginRsp>
        get() = PlayerLoginRsp.ADAPTER

    class Fail(
        private val retcode: Retcode
    ) : PlayerLoginRspPacket() {

        override fun buildProto(): PlayerLoginRsp =
            PlayerLoginRsp(
                retcode = this@Fail.retcode.value
            )
    }

    class Succ(
        private val queryCurrentRegionHttpRsp: QueryCurrRegionHttpRsp
    ) : PlayerLoginRspPacket() {
        override fun buildProto(): PlayerLoginRsp {

            val regionInfo = queryCurrentRegionHttpRsp.region_info

            return PlayerLoginRsp(
                is_use_ability_hash = true,
                ability_hash_code = -2044997239, // TODO: Unknown
                game_biz = "hk4e_global", // TODO: Hardcode
                client_silence_data_version = regionInfo?.client_silence_data_version ?: 0,
                client_data_version = regionInfo?.client_data_version ?: 0,
                client_version_suffix = regionInfo?.client_version_suffix ?: "",
                client_silence_md5 = regionInfo?.client_silence_data_md5 ?: "",
                register_cps = REGISTRY_CPS, // may be server provider
                res_version_config = regionInfo?.res_version_config,
                country_code = "US", // TODO: Hardcode
                client_md5 = regionInfo?.client_data_md5 ?: "",
                // TODO: Unknown - totalTickTime
                is_sc_open = false, // Anti-cheat?
                client_silence_version_suffix = regionInfo?.client_silence_version_suffix ?: "",
            )
        }
    }
}

internal class PingRspPacket(
    private val pingReq: PingReq
) : AbstractOutgoingPacket<PingRsp>(
    PacketId.PING_RSP
) {
    override fun buildProto(): PingRsp = PingRsp(client_time = pingReq.client_time)
    override val adapter: ProtoAdapter<PingRsp> get() = PingRsp.ADAPTER
}

internal class PlayerSetPauseRspPacket : AbstractOutgoingPacket<PlayerSetPauseRsp>(
    PacketId.PLAYER_SET_PAUSE_RSP
) {
    override fun buildProto(): PlayerSetPauseRsp = PlayerSetPauseRsp()
    override val adapter: ProtoAdapter<PlayerSetPauseRsp>
        get() = PlayerSetPauseRsp.ADAPTER
}

internal class DoSetPlayerBornDataNotifyPacket : AbstractOutgoingPacket<DoSetPlayerBornDataNotify>(
    PacketId.DO_SET_PLAYER_BORN_DATA_NOTIFY
) {

    override fun buildProto(): DoSetPlayerBornDataNotify =
        DoSetPlayerBornDataNotify()

    override val adapter: ProtoAdapter<DoSetPlayerBornDataNotify>
        get() = DoSetPlayerBornDataNotify.ADAPTER
}

internal class SetPlayerBornDataRspPacket : AbstractOutgoingPacket<SetPlayerBornDataRsp>(
    PacketId.SET_PLAYER_BORN_DATA_RSP
) {
    override fun buildProto(): SetPlayerBornDataRsp =
        SetPlayerBornDataRsp()

    override val adapter: ProtoAdapter<SetPlayerBornDataRsp>
        get() = SetPlayerBornDataRsp.ADAPTER
}

internal class ServerTimeNotifyPacket : AbstractOutgoingPacket<ServerTimeNotify>(
    PacketId.SERVER_TIME_NOTIFY
) {
    override fun buildProto(): ServerTimeNotify =
        ServerTimeNotify(server_time = nowMilliseconds())

    override val adapter: ProtoAdapter<ServerTimeNotify>
        get() = ServerTimeNotify.ADAPTER
}

internal class ServerDisconnectClientNotifyPacket : AbstractOutgoingPacket<ServerDisconnectClientNotify>(
    PacketId.SERVER_DISCONNECT_CLIENT_NOTIFY
) {
    override fun buildProto(): ServerDisconnectClientNotify = ServerDisconnectClientNotify()
    override val adapter: ProtoAdapter<ServerDisconnectClientNotify>
        get() = ServerDisconnectClientNotify.ADAPTER
}

// --- Player ---

internal class PlayerDataNotifyPacket(
    override val player: Player
) : PlayerOutgoingPacket<PlayerDataNotify>(
    PacketId.PLAYER_DATA_NOTIFY
) {

    override fun Player.buildProto(): PlayerDataNotify =
        PlayerDataNotify(
            nick_name = data.nickName,
            server_time = nowMilliseconds(),
            is_first_login_today = data.isFirstLoginToday,
            region_id = 1, // TODO: Hardcode
            prop_map = impl().playerProto.protoPropMap,
        )

    override val adapter: ProtoAdapter<PlayerDataNotify>
        get() = PlayerDataNotify.ADAPTER
}

internal class PlayerPropNotifyPacket(
    override val player: Player,
    val propMap: Map<Int, PropValue> = player.impl().playerProto.protoPropMap
) : PlayerOutgoingPacket<PlayerPropNotify>(
    PacketId.PLAYER_PROP_NOTIFY
) {
    override fun Player.buildProto(): PlayerPropNotify =
        PlayerPropNotify(prop_map = propMap)

    override val adapter: ProtoAdapter<PlayerPropNotify>
        get() = PlayerPropNotify.ADAPTER
}

internal class OpenStateUpdateNotifyPacket(
    override val player: Player
) : PlayerOutgoingPacket<OpenStateUpdateNotify>(
    PacketId.OPEN_STATE_UPDATE_NOTIFY
) {
    override fun Player.buildProto(): OpenStateUpdateNotify =
        OpenStateUpdateNotify(
            // TODO: For real situation
            open_state_map = OpenState.values().associate {
                // it.value to data.openState.contains(it)
                it.value to 1 // For test
            }
        )

    override val adapter: ProtoAdapter<OpenStateUpdateNotify>
        get() = OpenStateUpdateNotify.ADAPTER
}

internal class StoreWeightLimitNotifyPacket : AbstractOutgoingPacket<StoreWeightLimitNotify>(
    PacketId.STORE_WEIGHT_LIMIT_NOTIFY
) {

    override fun buildProto(): StoreWeightLimitNotify =
        StoreWeightLimitNotify(
            store_type = StoreType.STORE_TYPE_PACK,
            weight_limit = SorapointaConfig.data.inventoryLimits.allWeight,
            reliquary_count_limit = SorapointaConfig.data.inventoryLimits.reliquary,
            material_count_limit = SorapointaConfig.data.inventoryLimits.material,
            furniture_count_limit = SorapointaConfig.data.inventoryLimits.furniture,
        )

    override val adapter: ProtoAdapter<StoreWeightLimitNotify>
        get() = StoreWeightLimitNotify.ADAPTER
}

internal class PlayerStoreNotifyPacket(
    override val player: Player
) : PlayerOutgoingPacket<PlayerStoreNotify>(
    PacketId.PLAYER_STORE_NOTIFY
) {
    override fun Player.buildProto(): PlayerStoreNotify =
        PlayerStoreNotify(
            store_type = StoreType.STORE_TYPE_PACK,
            weight_limit = SorapointaConfig.data.inventoryLimits.allWeight,
            item_list = data.inventory.map { it.value.toProto() },
        )

    override val adapter: ProtoAdapter<PlayerStoreNotify>
        get() = PlayerStoreNotify.ADAPTER
}

internal class AvatarDataNotifyPacket(
    override val player: Player,
) : PlayerOutgoingPacket<AvatarDataNotify>(
    PacketId.AVATAR_DATA_NOTIFY
) {
    override fun Player.buildProto(): AvatarDataNotify =
        AvatarDataNotify(
            avatar_list = allAvatar.map { it.impl().avatarProto.toAvatarInfoProto() },
            avatar_team_map = data.compoundAvatarTeam.protoTeamMap,
            cur_avatar_team_id = data.selectedTeamId,
            choose_avatar_guid = data.selectedAvatarGuid,
            owned_flycloak_list = data.flyCloakSet.map { it.value },
            owned_costume_list = data.costumeSet.toList(),
        )

    override val adapter: ProtoAdapter<AvatarDataNotify>
        get() = AvatarDataNotify.ADAPTER
}

internal abstract class PlayerEnterSceneNotifyPacket : PlayerOutgoingPacket<PlayerEnterSceneNotify>(
    PacketId.PLAYER_ENTER_SCENE_NOTIFY
) {
    override val adapter: ProtoAdapter<PlayerEnterSceneNotify>
        get() = PlayerEnterSceneNotify.ADAPTER

    override fun Player.buildProto(): PlayerEnterSceneNotify = PlayerEnterSceneNotify(
        scene_id = scene.id,
        pos = data.position.toProto(),
        scene_begin_time = scene.beginTime,
        type = EnterType.ENTER_TYPE_SELF,
        target_uid = uid,
        enter_scene_token = getNextEnterSceneToken(),
        world_level = data.worldLevel,
        enter_reason = EnterReason.LOGIN.value,
        is_first_login_enter_scene = hasLoadedScene(scene.id),
        world_type = WorldType.WORLD_PLAYER.value, // TODO: Hardcode
        scene_transaction = "${scene.id}-$uid-${nowSeconds()}-$enterSceneToken", // TODO: last one is not token
    ).buildProto()

    open fun PlayerEnterSceneNotify.buildProto(): PlayerEnterSceneNotify = this

    class Login(
        override val player: Player
    ) : PlayerEnterSceneNotifyPacket()
}

internal class GetPlayerSocialDetailRspPacket(
    override val player: Player,
    private val targetUid: Int
) : PlayerOutgoingPacket<GetPlayerSocialDetailRsp>(
    PacketId.GET_PLAYER_SOCIAL_DETAIL_RSP
) {
    override fun Player.buildProto(): GetPlayerSocialDetailRsp {
        val data = PlayerDataImpl.findById(targetUid)
            ?: return GetPlayerSocialDetailRsp(retcode = Retcode.RET_SVR_ERROR.value)

        val onlinePlayer = Sorapointa.findPlayerById(this@GetPlayerSocialDetailRspPacket.targetUid)
        val isMpModeAvailable = onlinePlayer?.isMpModeAvailable ?: false
        val onlineState = if (onlinePlayer != null) {
            FriendOnlineState.FRIEND_ONLINE_STATE_ONLINE
        } else {
            FriendOnlineState.FRIEND_ONLINE_STATE_DISCONNECT
        }

        val social = SocialDetail(
            uid = this@GetPlayerSocialDetailRspPacket.targetUid,
            nickname = data.nickName,
            level = data.playerLevel,
            signature = data.signature,
            birthday = data.birthday.toProto(),
            world_level = data.worldLevel,
            is_mp_mode_available = isMpModeAvailable,
            online_state = onlineState,
            is_friend = PlayerFriendRelationTable.isFriendRelation(player.uid, targetUid),
            name_card_id = data.nameCardId,
            finish_achievement_num = 0, // TODO: Achievement System
            tower_floor_index = 1, // TODO: Spiral Abyss
            tower_level_index = 1,
            is_show_avatar = false, // TODO: Avatar Showcase
            show_name_card_id_list = data.nameCardSet.toList(), // TODO: Name Card Showcase
            profile_picture = data.profilePicture.toProto(),
        )

        return GetPlayerSocialDetailRsp(detail_data = social)
    }

    override val adapter: ProtoAdapter<GetPlayerSocialDetailRsp>
        get() = GetPlayerSocialDetailRsp.ADAPTER
}

internal class EnterScenePeerNotifyPacket(
    override val player: Player
) : PlayerOutgoingPacket<EnterScenePeerNotify>(
    PacketId.ENTER_SCENE_PEER_NOTIFY
) {
    override fun Player.buildProto(): EnterScenePeerNotify =
        EnterScenePeerNotify(
            peer_id = player.peerId,
            host_peer_id = player.world.hostPeerId,
            dest_scene_id = player.scene.id,
            enter_scene_token = player.enterSceneToken,
        )

    override val adapter: ProtoAdapter<EnterScenePeerNotify>
        get() = EnterScenePeerNotify.ADAPTER
}

internal abstract class EnterSceneReadyRspPacket : PlayerOutgoingPacket<EnterSceneReadyRsp>(
    PacketId.ENTER_SCENE_READY_RSP
) {
    override val adapter: ProtoAdapter<EnterSceneReadyRsp>
        get() = EnterSceneReadyRsp.ADAPTER

    class Succ(
        override val player: Player
    ) : EnterSceneReadyRspPacket() {
        override fun Player.buildProto(): EnterSceneReadyRsp =
            EnterSceneReadyRsp(enter_scene_token = player.enterSceneToken)
    }

    class Fail(
        override val player: Player,
        private val retcode: Retcode
    ) : EnterSceneReadyRspPacket() {

        override fun Player.buildProto(): EnterSceneReadyRsp =
            EnterSceneReadyRsp(retcode = this@Fail.retcode.value)
    }
}

internal abstract class SceneInitFinishRspPacket : PlayerOutgoingPacket<SceneInitFinishRsp>(
    PacketId.SCENE_INIT_FINISH_RSP
) {
    override val adapter: ProtoAdapter<SceneInitFinishRsp>
        get() = SceneInitFinishRsp.ADAPTER

    class Succ(
        override val player: Player
    ) : SceneInitFinishRspPacket() {
        override fun Player.buildProto(): SceneInitFinishRsp =
            SceneInitFinishRsp(enter_scene_token = player.enterSceneToken)
    }

    class Fail(
        override val player: Player,
        private val retcode: Retcode
    ) : SceneInitFinishRspPacket() {
        override fun Player.buildProto(): SceneInitFinishRsp =
            SceneInitFinishRsp(retcode = this@Fail.retcode.value)
    }
}

// --- Avatar ---

internal class AvatarFightPropUpdateNotifyPacket(
    override val avatar: Avatar,
    val propMap: Map<Int, Float>
) : AvatarOutgoingPacket<AvatarFightPropUpdateNotify>(
    PacketId.AVATAR_FIGHT_PROP_UPDATE_NOTIFY
) {
    override fun Avatar.buildProto(): AvatarFightPropUpdateNotify =
        AvatarFightPropUpdateNotify(
            avatar_guid = guid,
            fight_prop_map = propMap,
        )

    override val adapter: ProtoAdapter<AvatarFightPropUpdateNotify>
        get() = AvatarFightPropUpdateNotify.ADAPTER
}

internal class AvatarPropNotifyPacket(
    override val avatar: Avatar,
    val propMap: Map<Int, Long> = avatar.impl().avatarProto.protoPropMap.toFlattenPropMap()
) : AvatarOutgoingPacket<AvatarPropNotify>(
    PacketId.AVATAR_PROP_NOTIFY
) {
    override fun Avatar.buildProto(): AvatarPropNotify =
        AvatarPropNotify(avatar_guid = guid, prop_map = propMap)

    override val adapter: ProtoAdapter<AvatarPropNotify>
        get() = AvatarPropNotify.ADAPTER
}
