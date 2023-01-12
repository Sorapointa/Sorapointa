package org.sorapointa.server.network

import com.squareup.wire.ProtoAdapter
import io.ktor.util.*
import okio.ByteString.Companion.toByteString
import org.sorapointa.CoreBundle
import org.sorapointa.Sorapointa
import org.sorapointa.SorapointaConfig
import org.sorapointa.crypto.CryptoConfig
import org.sorapointa.dataloader.common.EnterReason
import org.sorapointa.dataloader.common.EntityIdType
import org.sorapointa.dataloader.common.OpenState
import org.sorapointa.dataloader.common.WorldType
import org.sorapointa.game.*
import org.sorapointa.game.data.PlayerFriendRelationTable
import org.sorapointa.game.impl
import org.sorapointa.proto.*
import org.sorapointa.proto.bin.PlayerDataBin
import org.sorapointa.utils.nowMilliseconds
import org.sorapointa.utils.nowSeconds
import org.sorapointa.utils.randomByteArray

// --- Session ---

internal abstract class GetPlayerTokenRspPacket : AbstractOutgoingPacket<GetPlayerTokenRsp>(
    PacketId.GET_PLAYER_TOKEN_RSP,
) {

    override val adapter: ProtoAdapter<GetPlayerTokenRsp> = GetPlayerTokenRsp.ADAPTER

    internal class Err(
        private val retcode: Retcode,
        private val msg: String,
    ) : GetPlayerTokenRspPacket() {
        override fun buildProto(): GetPlayerTokenRsp =
            GetPlayerTokenRsp(
                msg = CoreBundle.message(this@Err.msg),
                retcode = this@Err.retcode.value,
            )
    }

    internal class Succ(
        private val tokenReq: GetPlayerTokenReq,
        private val ip: String,
        private val encryptedServerSeed: String,
        private val sign: String,
    ) : GetPlayerTokenRspPacket() {
        override fun buildProto(): GetPlayerTokenRsp =
            GetPlayerTokenRsp(
                uid = tokenReq.account_uid.toInt(),
                token = tokenReq.account_token,
                account_type = tokenReq.account_type,
                account_uid = tokenReq.account_uid,
                is_proficient_player = true,
                key_id = CryptoConfig.data.useKeyId,
                server_rand_key = encryptedServerSeed,
                sign = sign,
                security_cmd_buffer = randomByteArray(32).toByteString(),
                platform_type = tokenReq.platform_type,
                channel_id = tokenReq.channel_id,
                sub_channel_id = tokenReq.sub_channel_id,
                country_code = "US",
                client_version_random_key = "a38-cc982d8bddd0",
                reg_platform = 1,
                client_ip_str = ip,
            )
    }
}

private val REGISTRY_CPS = "bWlob3lv".decodeBase64String()

internal abstract class PlayerLoginRspPacket : AbstractOutgoingPacket<PlayerLoginRsp>(
    PacketId.PLAYER_LOGIN_RSP
) {
    override val adapter: ProtoAdapter<PlayerLoginRsp> = PlayerLoginRsp.ADAPTER

    class Err(
        private val retcode: Retcode
    ) : PlayerLoginRspPacket() {

        override fun buildProto(): PlayerLoginRsp =
            PlayerLoginRsp(
                retcode = this@Err.retcode.value
            )
    }

    class Succ(
        private val queryCurrentRegionHttpRsp: QueryCurrRegionHttpRsp
    ) : PlayerLoginRspPacket() {
        override fun buildProto(): PlayerLoginRsp {

            val regionInfo = queryCurrentRegionHttpRsp.region_info

            return PlayerLoginRsp(
                is_use_ability_hash = true,
                ability_hash_code = -1793064043, // TODO: Unknown
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
    override val adapter: ProtoAdapter<PingRsp> = PingRsp.ADAPTER
}

internal class PlayerSetPauseRspPacket : AbstractOutgoingPacket<PlayerSetPauseRsp>(
    PacketId.PLAYER_SET_PAUSE_RSP
) {
    override fun buildProto(): PlayerSetPauseRsp = PlayerSetPauseRsp()
    override val adapter: ProtoAdapter<PlayerSetPauseRsp> = PlayerSetPauseRsp.ADAPTER
}

internal class DoSetPlayerBornDataNotifyPacket : AbstractOutgoingPacket<DoSetPlayerBornDataNotify>(
    PacketId.DO_SET_PLAYER_BORN_DATA_NOTIFY
) {

    override fun buildProto(): DoSetPlayerBornDataNotify =
        DoSetPlayerBornDataNotify()

    override val adapter: ProtoAdapter<DoSetPlayerBornDataNotify> = DoSetPlayerBornDataNotify.ADAPTER
}

internal class SetPlayerBornDataRspPacket : AbstractOutgoingPacket<SetPlayerBornDataRsp>(
    PacketId.SET_PLAYER_BORN_DATA_RSP
) {
    override fun buildProto(): SetPlayerBornDataRsp =
        SetPlayerBornDataRsp()

    override val adapter: ProtoAdapter<SetPlayerBornDataRsp> = SetPlayerBornDataRsp.ADAPTER
}

internal class ServerTimeNotifyPacket : AbstractOutgoingPacket<ServerTimeNotify>(
    PacketId.SERVER_TIME_NOTIFY
) {
    override fun buildProto(): ServerTimeNotify =
        ServerTimeNotify(server_time = nowMilliseconds())

    override val adapter: ProtoAdapter<ServerTimeNotify> = ServerTimeNotify.ADAPTER
}

internal class ServerDisconnectClientNotifyPacket : AbstractOutgoingPacket<ServerDisconnectClientNotify>(
    PacketId.SERVER_DISCONNECT_CLIENT_NOTIFY
) {
    override fun buildProto(): ServerDisconnectClientNotify = ServerDisconnectClientNotify()
    override val adapter: ProtoAdapter<ServerDisconnectClientNotify> = ServerDisconnectClientNotify.ADAPTER
}

// --- Player ---

internal class PlayerDataNotifyPacket(
    override val player: Player
) : PlayerOutgoingPacket<PlayerDataNotify>(
    PacketId.PLAYER_DATA_NOTIFY
) {

    override fun Player.buildProto(): PlayerDataNotify =
        PlayerDataNotify(
            nick_name = basicComp.nickname,
            server_time = nowMilliseconds(),
            is_first_login_today = basicComp.isFirstLoginToday,
            region_id = 1, // TODO: Hardcode
            prop_map = impl().playerProto.protoPropMap,
        )

    override val adapter: ProtoAdapter<PlayerDataNotify> = PlayerDataNotify.ADAPTER
}

internal class PlayerPropNotifyPacket(
    override val player: Player,
    val propMap: Map<Int, PropValue> = player.impl().playerProto.protoPropMap
) : PlayerOutgoingPacket<PlayerPropNotify>(
    PacketId.PLAYER_PROP_NOTIFY
) {
    override fun Player.buildProto(): PlayerPropNotify =
        PlayerPropNotify(prop_map = propMap)

    override val adapter: ProtoAdapter<PlayerPropNotify> = PlayerPropNotify.ADAPTER
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

    override val adapter: ProtoAdapter<OpenStateUpdateNotify> = OpenStateUpdateNotify.ADAPTER
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

    override val adapter: ProtoAdapter<StoreWeightLimitNotify> = StoreWeightLimitNotify.ADAPTER
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
            item_list = itemComp.packStore.getAllItems().map { it.toProto() },
        )

    override val adapter: ProtoAdapter<PlayerStoreNotify> = PlayerStoreNotify.ADAPTER
}

internal class AvatarDataNotifyPacket(
    override val player: Player,
) : PlayerOutgoingPacket<AvatarDataNotify>(
    PacketId.AVATAR_DATA_NOTIFY
) {
    override fun Player.buildProto(): AvatarDataNotify =
        AvatarDataNotify(
            avatar_list = avatarComp.getAvatarList().map { it.avatarProto.toAvatarInfoProto() },
            avatar_team_map = avatarComp.team.getAvatarTeamMapProto(),
            cur_avatar_team_id = avatarComp.team.curTeamId,
            choose_avatar_guid = avatarComp.curAvatarGuid,
            owned_flycloak_list = avatarComp.getOwnedFlycloakList().map { it.value },
            owned_costume_list = avatarComp.getOwnedCostumeIdList(),
        )

    override val adapter: ProtoAdapter<AvatarDataNotify> = AvatarDataNotify.ADAPTER
}

internal abstract class PlayerEnterSceneNotifyPacket : PlayerOutgoingPacket<PlayerEnterSceneNotify>(
    PacketId.PLAYER_ENTER_SCENE_NOTIFY
) {
    override val adapter: ProtoAdapter<PlayerEnterSceneNotify> = PlayerEnterSceneNotify.ADAPTER

    override fun Player.buildProto(): PlayerEnterSceneNotify = PlayerEnterSceneNotify(
        scene_id = scene.id,
        pos = avatarComp.pbOnlyCurPos.toProto(),
        scene_begin_time = scene.beginTime,
        type = EnterType.ENTER_TYPE_SELF,
        target_uid = uid,
        enter_scene_token = getNextEnterSceneToken(),
        world_level = sceneComp.world.level,
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

internal abstract class GetPlayerSocialDetailRspPacket(
    override val player: Player,
) : PlayerOutgoingPacket<GetPlayerSocialDetailRsp>(
    PacketId.GET_PLAYER_SOCIAL_DETAIL_RSP
) {

    class Err(
        override val player: Player,
        private val retcode: Retcode,
    ) : GetPlayerSocialDetailRspPacket(player) {

        override fun Player.buildProto(): GetPlayerSocialDetailRsp =
            GetPlayerSocialDetailRsp(
                retcode = this@Err.retcode.value
            )
    }

    class SuccOnline(
        override val player: Player,
        private val targetPlayer: Player
    ) : GetPlayerSocialDetailRspPacket(player) {

        override fun Player.buildProto(): GetPlayerSocialDetailRsp {
            val isMpModeAvailable = player.isMpModeAvailable

            val basicComp = targetPlayer.basicComp
            val socialComp = targetPlayer.socialComp

            val social = SocialDetail(
                uid = targetPlayer.uid,
                nickname = basicComp.nickname,
                level = basicComp.level,
                signature = socialComp.signature,
                birthday = socialComp.birthday.toProto(),
                world_level = targetPlayer.sceneComp.world.level,
                is_mp_mode_available = isMpModeAvailable,
                online_state = FriendOnlineState.FRIEND_ONLINE_STATE_ONLINE,
                is_friend = PlayerFriendRelationTable.isFriendRelation(player.uid, targetPlayer.uid),
                name_card_id = socialComp.nameCardId,
                finish_achievement_num = 0, // TODO: Achievement System
                tower_floor_index = 1, // TODO: Spiral Abyss
                tower_level_index = 1,
                is_show_avatar = false, // TODO: Avatar Showcase
                show_name_card_id_list = socialComp.getShowNameCardIdList(),
                profile_picture = basicComp.getProfilePictureProto(),
            )

            return GetPlayerSocialDetailRsp(detail_data = social)
        }
    }

    class SuccOffline(
        override val player: Player,
        private val targetUid: Int,
        private val targetPlayerData: PlayerDataBin,
    ) : GetPlayerSocialDetailRspPacket(player) {

        override fun Player.buildProto(): GetPlayerSocialDetailRsp {
            val onlinePlayer = Sorapointa.findOrNullPlayerById(targetUid)
            val isMpModeAvailable = onlinePlayer?.isMpModeAvailable ?: false
            val onlineState = if (onlinePlayer != null) {
                FriendOnlineState.FRIEND_ONLINE_STATE_ONLINE
            } else {
                FriendOnlineState.FRIEND_ONLINE_STATE_DISCONNECT
            }

            val basicBin = targetPlayerData.basic_bin!!
            val socialBin = targetPlayerData.social_bin!!

            val social = SocialDetail(
                uid = targetUid,
                nickname = basicBin.nickname,
                level = basicBin.level,
                signature = socialBin.signature,
                birthday = socialBin.birthday?.toProto(),
                world_level = targetPlayerData.scene_bin?.world?.level ?: 0,
                is_mp_mode_available = isMpModeAvailable,
                online_state = onlineState,
                is_friend = PlayerFriendRelationTable.isFriendRelation(player.uid, targetUid),
                name_card_id = socialBin.name_card_id,
                finish_achievement_num = 0, // TODO: Achievement System
                tower_floor_index = 1, // TODO: Spiral Abyss
                tower_level_index = 1,
                is_show_avatar = false, // TODO: Avatar Showcase
                show_name_card_id_list = socialBin.show_name_card_id_list,
                profile_picture = ProfilePicture(
                    avatar_id = basicBin.head_image_avatar_id,
                    costume_id = basicBin.profile_picture_costume_id,
                ),
            )

            return GetPlayerSocialDetailRsp(detail_data = social)
        }
    }

    override val adapter: ProtoAdapter<GetPlayerSocialDetailRsp> = GetPlayerSocialDetailRsp.ADAPTER
}

internal class SceneEntityAppearNotifyPacket(
    override val player: Player
) : PlayerOutgoingPacket<SceneEntityAppearNotify>(
    PacketId.SCENE_ENTITY_APPEAR_NOTIFY
) {

    override fun Player.buildProto(): SceneEntityAppearNotify {
        return SceneEntityAppearNotify(
            entity_list = scene.entities.values.map { it.entityProto.toProto() },
            appear_type = VisionType.VISION_TYPE_BORN,
        )
    }

    override val adapter: ProtoAdapter<SceneEntityAppearNotify> = SceneEntityAppearNotify.ADAPTER
}

internal class SceneTeamUpdateNotifyPacket(
    override val player: Player
) : PlayerOutgoingPacket<SceneTeamUpdateNotify>(
    PacketId.SCENE_TEAM_UPDATE_NOTIFY
) {

    override fun Player.buildProto(): SceneTeamUpdateNotify {
        return SceneTeamUpdateNotify(
            scene_team_avatar_list = avatarComp.team.getSceneTeamAvatarListProto()
        )
    }

    override val adapter: ProtoAdapter<SceneTeamUpdateNotify> = SceneTeamUpdateNotify.ADAPTER
}

internal class PlayerEnterSceneInfoNotifyPacket(
    override val player: Player
) : PlayerOutgoingPacket<PlayerEnterSceneInfoNotify>(
    PacketId.PLAYER_ENTER_SCENE_INFO_NOTIFY
) {

    override fun Player.buildProto(): PlayerEnterSceneInfoNotify {
        return PlayerEnterSceneInfoNotify(
            enter_scene_token = player.enterSceneToken,
            cur_avatar_entity_id = player.avatarComp.getCurAvatar().id,
            team_enter_info = TeamEnterSceneInfo(
                team_ability_info = AbilitySyncStateInfo(),
                ability_control_block = AbilityControlBlock(),
                team_entity_id = getNextEntityId(EntityIdType.TEAM), // TODO: 随便糊的，以后改
            ),
            avatar_enter_info = player.avatarComp.team.getSelectedTeamAvatarList().map {
                AvatarEnterSceneInfo(
                    avatar_ability_info = AbilitySyncStateInfo(),
                    avatar_guid = it.avatar.guid,
                    avatar_entity_id = it.id,
                    weapon_ability_info = AbilitySyncStateInfo(),
                    weapon_guid = it.avatar.equipWeapon?.guid ?: 0,
                    weapon_entity_id = it.equipWeaponEntityId ?: 0,
                )
            },
            mp_level_entity_info = MPLevelEntityInfo(
                authority_peer_id = player.peerId,
                entity_id = getNextEntityId(EntityIdType.MPLEVEL), // TODO: 随便糊的，以后改
            ),
        )
    }

    override val adapter: ProtoAdapter<PlayerEnterSceneInfoNotify> = PlayerEnterSceneInfoNotify.ADAPTER
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

    override val adapter: ProtoAdapter<EnterScenePeerNotify> = EnterScenePeerNotify.ADAPTER
}

internal abstract class EnterSceneReadyRspPacket : PlayerOutgoingPacket<EnterSceneReadyRsp>(
    PacketId.ENTER_SCENE_READY_RSP
) {
    override val adapter: ProtoAdapter<EnterSceneReadyRsp> = EnterSceneReadyRsp.ADAPTER

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
    override val adapter: ProtoAdapter<SceneInitFinishRsp> = SceneInitFinishRsp.ADAPTER

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

internal abstract class EnterSceneDoneRspPacket : PlayerOutgoingPacket<EnterSceneDoneRsp>(
    PacketId.ENTER_SCENE_DONE_RSP
) {
    override val adapter: ProtoAdapter<EnterSceneDoneRsp> = EnterSceneDoneRsp.ADAPTER

    class Succ(
        override val player: Player
    ) : EnterSceneDoneRspPacket() {
        override fun Player.buildProto(): EnterSceneDoneRsp =
            EnterSceneDoneRsp(enter_scene_token = player.enterSceneToken)
    }

    class Fail(
        override val player: Player,
        private val retcode: Retcode
    ) : EnterSceneDoneRspPacket() {
        override fun Player.buildProto(): EnterSceneDoneRsp =
            EnterSceneDoneRsp(retcode = this@Fail.retcode.value)
    }
}

internal abstract class PostEnterSceneRspPacket : PlayerOutgoingPacket<PostEnterSceneRsp>(
    PacketId.POST_ENTER_SCENE_RSP
) {
    override val adapter: ProtoAdapter<PostEnterSceneRsp> = PostEnterSceneRsp.ADAPTER

    class Succ(
        override val player: Player
    ) : PostEnterSceneRspPacket() {
        override fun Player.buildProto(): PostEnterSceneRsp =
            PostEnterSceneRsp(enter_scene_token = player.enterSceneToken)
    }

    class Fail(
        override val player: Player,
        private val retcode: Retcode
    ) : PostEnterSceneRspPacket() {
        override fun Player.buildProto(): PostEnterSceneRsp =
            PostEnterSceneRsp(retcode = this@Fail.retcode.value)
    }
}

// --- Avatar ---

internal class AvatarFightPropUpdateNotifyPacket(
    override val avatarEntity: AvatarEntity,
    val propMap: Map<Int, Float>
) : AvatarOutgoingPacket<AvatarFightPropUpdateNotify>(
    PacketId.AVATAR_FIGHT_PROP_UPDATE_NOTIFY
) {
    override fun AvatarEntity.buildProto(): AvatarFightPropUpdateNotify =
        AvatarFightPropUpdateNotify(
            avatar_guid = avatar.guid,
            fight_prop_map = propMap,
        )

    override val adapter: ProtoAdapter<AvatarFightPropUpdateNotify> = AvatarFightPropUpdateNotify.ADAPTER
}

internal class AvatarPropNotifyPacket(
    override val avatarEntity: AvatarEntity,
    val propMap: Map<Int, Long> = avatarEntity.impl().avatarProto.protoPropMap.toFlattenPropMap()
) : AvatarOutgoingPacket<AvatarPropNotify>(
    PacketId.AVATAR_PROP_NOTIFY
) {
    override fun AvatarEntity.buildProto(): AvatarPropNotify =
        AvatarPropNotify(avatar_guid = avatar.guid, prop_map = propMap)

    override val adapter: ProtoAdapter<AvatarPropNotify> = AvatarPropNotify.ADAPTER
}
