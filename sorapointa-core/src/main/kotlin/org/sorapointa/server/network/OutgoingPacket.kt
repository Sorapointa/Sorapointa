package org.sorapointa.server.network

import com.google.protobuf.GeneratedMessageV3
import io.ktor.util.*
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
import org.sorapointa.proto.FriendOnlineStateOuterClass.FriendOnlineState
import org.sorapointa.proto.GetPlayerTokenReqOuterClass.GetPlayerTokenReq
import org.sorapointa.proto.PingReqOuterClass.PingReq
import org.sorapointa.proto.PropValueOuterClass.PropValue
import org.sorapointa.proto.QueryCurrRegionHttpRspOuterClass.QueryCurrRegionHttpRsp
import org.sorapointa.proto.RetcodeOuterClass.Retcode
import org.sorapointa.proto.StoreTypeOuterClass.StoreType
import org.sorapointa.utils.i18n
import org.sorapointa.utils.nowMilliseconds
import org.sorapointa.utils.nowSeconds
import org.sorapointa.utils.randomByteArray

// --- Session ---

internal abstract class GetPlayerTokenRspPacket : AbstractOutgoingPacket(
    PacketId.GET_PLAYER_TOKEN_RSP,
) {

    internal class Error(
        private val retcode: Retcode,
        private val msg: String
    ) : GetPlayerTokenRspPacket() {
        override fun buildProto(): GeneratedMessageV3 =
            getPlayerTokenRsp {
                msg = this@Error.msg.i18n()
                retcode = this@Error.retcode.number
            }
    }

    internal class Successful(
        private val tokenReq: GetPlayerTokenReq,
        private val keySeed: ULong,
        private val ip: String
    ) : GetPlayerTokenRspPacket() {
        override fun buildProto(): GeneratedMessageV3 =
            getPlayerTokenRsp {
                uid = tokenReq.accountUid.toInt()
                token = tokenReq.accountToken
                accountType = tokenReq.accountType
                accountUid = tokenReq.accountUid
                isProficientPlayer = true
                secretKeySeed = keySeed.toLong()
                securityCmdBuffer = randomByteArray(32).toByteString()
                platformType = tokenReq.platformType
                channelId = tokenReq.channelId
                subChannelId = tokenReq.subChannelId
                countryCode = "US"
                clientVersionRandomKey = "aeb-bc90f1631c05"
                regPlatform = 1
                clientIpStr = ip
            }
    }
}

private val REGISTRY_CPS = "bWlob3lv".decodeBase64String()

internal abstract class PlayerLoginRspPacket : AbstractOutgoingPacket(
    PacketId.PLAYER_LOGIN_RSP
) {

    class Fail(
        private val retcode: Retcode
    ) : PlayerLoginRspPacket() {

        override fun buildProto(): GeneratedMessageV3 =
            playerLoginRsp {
                retcode = this@Fail.retcode.number
            }
    }

    class Succ(
        private val queryCurrentRegionHttpRsp: QueryCurrRegionHttpRsp
    ) : PlayerLoginRspPacket() {
        override fun buildProto(): GeneratedMessageV3 =
            playerLoginRsp {
                val regionInfo = queryCurrentRegionHttpRsp.regionInfo
                isUseAbilityHash = true
                abilityHashCode = -2044997239 // TODO: Unknown
                gameBiz = "hk4e_global" // TODO: Hardcode
                clientSilenceDataVersion = regionInfo.clientSilenceDataVersion
                clientDataVersion = regionInfo.clientDataVersion
                clientVersionSuffix = regionInfo.clientVersionSuffix
                clientSilenceMd5 = regionInfo.clientSilenceDataMd5
                registerCps = REGISTRY_CPS // may be server provider
                resVersionConfig = regionInfo.resVersionConfig
                countryCode = "US" // TODO: Hardcode
                clientMd5 = regionInfo.clientDataMd5
                // TODO: Unknown - totalTickTime
                isScOpen = false // Anti-cheat?
                clientSilenceVersionSuffix = regionInfo.clientSilenceVersionSuffix
//            totalTickTime
            }
    }
}

internal class PingRspPacket(
    private val pingReq: PingReq
) : AbstractOutgoingPacket(
    PacketId.PING_RSP
) {

    override fun buildProto(): GeneratedMessageV3 =
        pingRsp {
            clientTime = pingReq.clientTime
        }
}

internal class PlayerSetPauseRspPacket : AbstractOutgoingPacket(
    PacketId.PLAYER_SET_PAUSE_RSP
) {

    override fun buildProto(): GeneratedMessageV3 =
        playerSetPauseRsp { }
}

internal class DoSetPlayerBornDataNotifyPacket : AbstractOutgoingPacket(
    PacketId.DO_SET_PLAYER_BORN_DATA_NOTIFY
) {

    override fun buildProto(): GeneratedMessageV3 =
        doSetPlayerBornDataNotify { }
}

internal class SetPlayerBornDataRspPacket : AbstractOutgoingPacket(
    PacketId.SET_PLAYER_BORN_DATA_RSP
) {

    override fun buildProto(): GeneratedMessageV3 =
        setPlayerBornDataRsp { }
}

internal class ServerTimeNotifyPacket : AbstractOutgoingPacket(
    PacketId.SERVER_TIME_NOTIFY
) {

    override fun buildProto(): GeneratedMessageV3 =
        serverTimeNotify {
            serverTime = nowMilliseconds()
        }

}


// --- Player ---

internal class PlayerDataNotifyPacket(
    override val player: Player
) : PlayerOutgoingPacket(
    PacketId.PLAYER_DATA_NOTIFY
) {

    override fun Player.buildProto(): GeneratedMessageV3 =
        playerDataNotify {
            nickName = data.nickName
            serverTime = nowMilliseconds()
            isFirstLoginToday = data.isFirstLoginToday
            regionId = 1 // TODO: Hardcode
            propMap.putAll(impl().playerProto.protoPropMap)
        }
}

internal class PlayerPropNotifyPacket(
    override val player: Player,
    val propMap: Map<Int, PropValue> = player.impl().playerProto.protoPropMap
) : PlayerOutgoingPacket(
    PacketId.PLAYER_PROP_NOTIFY
) {

    override fun Player.buildProto(): GeneratedMessageV3 =
        playerPropNotify {
            propMap.putAll(this@PlayerPropNotifyPacket.propMap)
        }
}

internal class OpenStateUpdateNotifyPacket(
    override val player: Player
) : PlayerOutgoingPacket(
    PacketId.OPEN_STATE_UPDATE_NOTIFY
) {
    override fun Player.buildProto(): GeneratedMessageV3 =
        openStateUpdateNotify {
            this.openStateMap.putAll(
                OpenState.values().associate {
//                    it.value to data.openState.contains(it) // TODO: For real situation
                    it.value to 1 // For test
                }
            )
        }
}

internal class StoreWeightLimitNotifyPacket : AbstractOutgoingPacket(PacketId.STORE_WEIGHT_LIMIT_NOTIFY) {

    override fun buildProto(): GeneratedMessageV3 =
        storeWeightLimitNotify {
            storeType = StoreType.STORE_TYPE_PACK
            weightLimit = SorapointaConfig.data.inventoryLimits.allWeight
            reliquaryCountLimit = SorapointaConfig.data.inventoryLimits.reliquary
            materialCountLimit = SorapointaConfig.data.inventoryLimits.material
            furnitureCountLimit = SorapointaConfig.data.inventoryLimits.furniture
        }
}

internal class PlayerStoreNotifyPacket(
    override val player: Player
) : PlayerOutgoingPacket(
    PacketId.PLAYER_STORE_NOTIFY
) {

    override fun Player.buildProto(): GeneratedMessageV3 =
        playerStoreNotify {
            storeType = StoreType.STORE_TYPE_PACK
            weightLimit = SorapointaConfig.data.inventoryLimits.allWeight
            itemList.addAll(data.inventory.map { it.value.toProto() })
        }
}

internal class AvatarDataNotifyPacket(
    override val player: Player,
) : PlayerOutgoingPacket(
    PacketId.AVATAR_DATA_NOTIFY
) {

    override fun Player.buildProto(): GeneratedMessageV3 =
        avatarDataNotify {
            avatarList.addAll(allAvatar.map { it.impl().avatarProto.toAvatarInfoProto() })
            avatarTeamMap.putAll(data.compoundAvatarTeam.protoTeamMap)
            curAvatarTeamId = data.selectedTeamId
            chooseAvatarGuid = data.selectedAvatarGuid
            ownedFlycloakList.addAll(data.flyCloakSet.map { it.id })
            ownedCostumeList.addAll(data.costumeSet)
        }
}

internal abstract class PlayerEnterSceneNotifyPacket : PlayerOutgoingPacket(
    PacketId.PLAYER_ENTER_SCENE_NOTIFY
) {

    override fun Player.buildProto(): GeneratedMessageV3 {
        return playerEnterSceneNotify {
            sceneId = scene.id
            pos = data.position.toProto()
            sceneBeginTime = scene.beginTime
            type = EnterTypeOuterClass.EnterType.ENTER_TYPE_SELF
            targetUid = uid
            enterSceneToken = getNextEnterSceneToken()
            worldLevel = data.worldLevel
            enterReason = EnterReason.LOGIN.value
            isFirstLoginEnterScene = hasLoadedScene(scene.id)
            // TODO: sceneTagIdList - SceneTagConfigData - Scene special decoration, such like HDJ (Spring Festival)
            worldType = WorldType.WORLD_PLAYER.value // TODO: Hardcode
            sceneTransaction = "${scene.id}-$uid-${nowSeconds()}-$enterSceneToken" // TODO: last one is not token
            buildProto()
        }
    }

    open fun PlayerEnterSceneNotifyKt.Dsl.buildProto() {

    }

    class Login(
        override val player: Player
    ): PlayerEnterSceneNotifyPacket()
}

internal class GetPlayerSocialDetailRspPacket(
    override val player: Player,
    private val targetUid: Int
) : PlayerOutgoingPacket(
    PacketId.GET_PLAYER_SOCIAL_DETAIL_RSP
) {

    override fun Player.buildProto(): GeneratedMessageV3 =
        PlayerDataImpl.findById(targetUid)?.let { data ->
            getPlayerSocialDetailRsp {
                detailData = socialDetail {
                    uid = this@GetPlayerSocialDetailRspPacket.targetUid
                    nickname = data.nickName
                    level = data.playerLevel
                    signature = data.signature
                    birthday = data.birthday.toProto()
                    worldLevel = data.worldLevel
                    val onlinePlayer = Sorapointa.findPlayerById(this@GetPlayerSocialDetailRspPacket.targetUid)
                    if (onlinePlayer != null) {
                        isMpModeAvailable = onlinePlayer.isMpModeAvailable
                        onlineState = FriendOnlineState.FRIEND_ONLINE_STATE_ONLINE
                    } else {
                        isMpModeAvailable = false
                        onlineState = FriendOnlineState.FRIEND_ONLINE_STATE_FREIEND_DISCONNECT
                    }
                    isFriend = PlayerFriendRelationTable.isFriendRelation(player.uid, targetUid)
                    nameCardId = data.nameCardId
                    finishAchievementNum = 0 // TODO: Achievement System
                    towerFloorIndex = 1 // TODO: Spiral Abyss
                    towerLevelIndex = 1
                    isShowAvatar = false // TODO: Avatar Showcase
                    showNameCardIdList.addAll(data.nameCardSet) // TODO: Name Card Showcase
                    profilePicture = data.profilePicture.toProto()
                }
            }
        } ?: getPlayerSocialDetailRsp {
            retcode = Retcode.RETCODE_RET_SVR_ERROR.number
        }

}

internal class EnterScenePeerNotifyPacket(
    override val player: Player
) : PlayerOutgoingPacket(
    PacketId.ENTER_SCENE_PEER_NOTIFY
) {

    override fun Player.buildProto(): GeneratedMessageV3 =
        enterScenePeerNotify {
            peerId = player.peerId
            hostPeerId = player.world.hostPeerId
            destSceneId = player.scene.id
            enterSceneToken = player.enterSceneToken
        }

}

internal abstract class EnterSceneReadyRspPacket: PlayerOutgoingPacket(
    PacketId.ENTER_SCENE_READY_RSP
) {

    class Succ(
        override val player: Player
    ): EnterSceneReadyRspPacket() {

        override fun Player.buildProto(): GeneratedMessageV3 =
            enterSceneReadyRsp {
                enterSceneToken = player.enterSceneToken
            }
    }

    class Fail(
        override val player: Player,
        private val retcode: Retcode
    ): EnterSceneReadyRspPacket() {

        override fun Player.buildProto(): GeneratedMessageV3 =
            enterSceneReadyRsp {
                retcode = this@Fail.retcode.number
            }

    }

}

internal abstract class SceneInitFinishRspPacket: PlayerOutgoingPacket(
    PacketId.SCENE_INIT_FINISH_RSP
) {

    class Succ(
        override val player: Player
    ): SceneInitFinishRspPacket() {

        override fun Player.buildProto(): GeneratedMessageV3 =
            sceneInitFinishRsp {
                enterSceneToken = player.enterSceneToken
            }
    }

    class Fail(
        override val player: Player,
        private val retcode: Retcode
    ): SceneInitFinishRspPacket() {

        override fun Player.buildProto(): GeneratedMessageV3 =
            enterSceneReadyRsp {
                retcode = this@Fail.retcode.number
            }

    }

}

// --- Avatar ---

internal class AvatarFightPropUpdateNotifyPacket(
    override val avatar: Avatar,
    val propMap: Map<Int, Float>
) : AvatarOutgoingPacket(
    PacketId.AVATAR_FIGHT_PROP_UPDATE_NOTIFY
) {

    override fun Avatar.buildProto(): GeneratedMessageV3 =
        avatarFightPropUpdateNotify {
            avatarGuid = guid
            fightPropMap.putAll(propMap)
        }
}

internal class AvatarPropNotifyPacket(
    override val avatar: Avatar,
    val propMap: Map<Int, Long> = avatar.impl().avatarProto.protoPropMap.toFlattenPropMap()
) : AvatarOutgoingPacket(
    PacketId.AVATAR_PROP_NOTIFY
) {

    override fun Avatar.buildProto(): GeneratedMessageV3 =
        avatarPropNotify {
            avatarGuid = guid
            propMap.putAll(this@AvatarPropNotifyPacket.propMap)
        }
}
