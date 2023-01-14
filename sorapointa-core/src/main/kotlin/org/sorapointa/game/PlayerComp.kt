package org.sorapointa.game

import kotlinx.atomicfu.atomic
import kotlinx.datetime.Instant
import org.sorapointa.events.PlayerLoginEvent
import org.sorapointa.game.data.BIG_WORLD_SCENE_ID
import org.sorapointa.game.data.DEFAULT_NAME_CARD
import org.sorapointa.game.data.MAX_STAMINA
import org.sorapointa.proto.ProfilePicture
import org.sorapointa.proto.bin.*
import org.sorapointa.server.network.PlayerDataNotifyPacket
import org.sorapointa.utils.getNextGuid
import org.sorapointa.utils.now
import org.sorapointa.utils.nowSeconds
import org.sorapointa.utils.todayStartTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import org.sorapointa.proto.Birthday as BirthdayProto

interface PlayerModule {

    val player: Player
}

class PlayerBasicComp(
    override val player: Player,
    private val initPlayerBasicCompBin: PlayerBasicCompBin,
) : PlayerModule {

    companion object {

        fun createNew(nickname: String, pickAvatarId: Int): PlayerBasicCompBin {
            val now = nowSeconds().toUInt().toInt()
            return PlayerBasicCompBin(
                level = 1,
                exp = 0,
                nickname = nickname,
                persist_stamina_limit = MAX_STAMINA.toFloat(),
                cur_persist_stamina = MAX_STAMINA.toFloat(),
                cur_temporary_stamina = 0f,
                head_image_avatar_id = pickAvatarId,
                guid_seq_id = 0,
                last_login_time = now,
                register_time = now,
            )
        }
    }

    val level
        get() = _level.value
    val exp
        get() = _exp.value
    val nickname
        get() = _nickname.value
    val persistStaminaLimit
        get() = _persistStaminaLimit.value
    val curPersistStamina
        get() = _curPersistStamina.value
    val curTemporaryStamina
        get() = _curTemporaryStamina.value
    val headImageAvatarId
        get() = _headImageAvatarId.value
    val profilePictureCostumeId
        get() = _profilePictureCostumeId.value

    private val guidSeqId
        get() = _guidSeqId.value

    val registerTime = initPlayerBasicCompBin.register_time
    val openStateMap = ConcurrentHashMap(initPlayerBasicCompBin.open_state_map)

    val lastLoginTime
        get() = Instant.fromEpochSeconds(_lastLoginTime.value.toLong())

    val daysSinceLastLogin get() = (now() - lastLoginTime).inWholeDays

    val isFirstLoginToday get() = lastLoginTime < todayStartTime

    private var _level = atomic(initPlayerBasicCompBin.level)
    private var _exp = atomic(initPlayerBasicCompBin.exp)
    private var _nickname = atomic(initPlayerBasicCompBin.nickname)
    private var _lastLoginTime = atomic(initPlayerBasicCompBin.last_login_time)
    private var _persistStaminaLimit = atomic(initPlayerBasicCompBin.persist_stamina_limit)
    private var _curPersistStamina = atomic(initPlayerBasicCompBin.cur_persist_stamina)
    private var _curTemporaryStamina = atomic(initPlayerBasicCompBin.cur_temporary_stamina)
    private var _headImageAvatarId = atomic(initPlayerBasicCompBin.head_image_avatar_id)
    private var _profilePictureCostumeId = atomic(initPlayerBasicCompBin.profile_picture_costume_id)
    private var _guidSeqId = atomic(initPlayerBasicCompBin.guid_seq_id)

    internal fun init() {
        player.registerEventListener<PlayerLoginEvent> {
            player.impl().sendPacket(PlayerDataNotifyPacket(player))
        }
    }

    fun updateLastLoginTime() {
        _lastLoginTime.value = nowSeconds().toInt()
    }

    fun updateNickname(nickname: String) {
        _nickname.value = nickname
    }

    internal fun getNextGuid(): Long {
        return _guidSeqId.getAndIncrement().getNextGuid(player.uid)
    }

    fun toBin(): PlayerBasicCompBin {
        return PlayerBasicCompBin(
            level = level,
            exp = exp,
            nickname = nickname,
            persist_stamina_limit = persistStaminaLimit,
            cur_persist_stamina = curPersistStamina,
            cur_temporary_stamina = curTemporaryStamina,
            head_image_avatar_id = headImageAvatarId,
            guid_seq_id = guidSeqId,
            last_login_time = lastLoginTime.epochSeconds.toInt(),
            register_time = registerTime,
            open_state_map = openStateMap,
        )
    }

    fun getProfilePictureProto() =
        ProfilePicture(
            avatar_id = headImageAvatarId,
            costume_id = profilePictureCostumeId,
        )
}

class PlayerSceneComp(
    override val player: Player,
    private val initPlayerSceneBin: PlayerSceneCompBin,
) : PlayerModule {

    companion object {

        internal fun createNew(uid: Int): PlayerSceneCompBin {
            return PlayerSceneCompBin(
                world = PlayerWorldData.createNew(),
                cur_scene_owner_uid = uid,
                my_cur_scene_id = BIG_WORLD_SCENE_ID,
            )
        }
    }

    internal fun init() {
    }

    val world = PlayerWorldData(
        this,
        initPlayerSceneBin.world ?: PlayerWorldData.createNew(),
    )
    val curSceneOwnerUid = initPlayerSceneBin.cur_scene_owner_uid
    val myPrevSceneId = initPlayerSceneBin.my_prev_scene_id
    val myPrevPos = initPlayerSceneBin.my_prev_pos
    val myPrevRot = initPlayerSceneBin.my_prev_rot
    val myCurSceneId = initPlayerSceneBin.my_cur_scene_id

    internal fun toBin(): PlayerSceneCompBin {
        return PlayerSceneCompBin(
            world = world.toBin(),
            cur_scene_owner_uid = curSceneOwnerUid,
            my_prev_scene_id = myPrevSceneId,
            my_prev_pos = myPrevPos,
            my_prev_rot = myPrevRot,
            my_cur_scene_id = myCurSceneId,
        )
    }
}

class PlayerWorldData(
    val playerSceneComp: PlayerSceneComp,
    private val initWorldBin: WorldBin,
) {

    companion object {

        internal fun createNew(): WorldBin {
            return WorldBin(
                scene_map = emptyMap(),
                level = 1,
            )
        }
    }

    private val sceneMap = ConcurrentHashMap(initWorldBin.scene_map)
    val level = initWorldBin.level
    val lastAdjustTime = initWorldBin.last_adjust_time
    val adjustLevel = initWorldBin.adjust_level

    internal fun toBin(): WorldBin {
        return WorldBin(
            scene_map = sceneMap,
            level = level,
            last_adjust_time = lastAdjustTime,
            adjust_level = adjustLevel,
        )
    }
}

class PlayerSocialComp(
    override val player: Player,
    private val initPlayerSocialBin: PlayerSocialCompBin,
) : PlayerModule {

    companion object {

        internal fun createNew(): PlayerSocialCompBin {
            return PlayerSocialCompBin(
                unlock_name_card_list = listOf(DEFAULT_NAME_CARD),
                birthday = Birthday(1, 1),
                signature = "Hi, Sorapointa",
                name_card_id = DEFAULT_NAME_CARD,
            )
        }
    }

    internal fun init() {
//        player.registerEventListener<PlayerLoginEvent> {
//        }
    }

    private val unlockNameCardList = ConcurrentLinkedQueue(initPlayerSocialBin.unlock_name_card_list)
    val birthday = initPlayerSocialBin.birthday ?: Birthday(1, 1)
    val signature = initPlayerSocialBin.signature
    val nameCardId = initPlayerSocialBin.name_card_id
    private val recentMpPlayerUidList = ConcurrentLinkedQueue(initPlayerSocialBin.recent_mp_player_uid_list)
    val lastGrantBirthdayBenefitYear = initPlayerSocialBin.last_grant_birthday_benefit_year // 应该是指上次领取生日礼包的年份
    val isHaveFirstShare = initPlayerSocialBin.is_have_first_share
    val isLoginOnBirthday = initPlayerSocialBin.is_login_on_birthday
    private val friendRemarkNameMap = ConcurrentHashMap(initPlayerSocialBin.friend_remark_name_map)
    private val showAvatarIdList = ConcurrentLinkedQueue(initPlayerSocialBin.show_avatar_id_list)
    val isShowAvatar = initPlayerSocialBin.is_show_avatar
    private val showNameCardIdList = ConcurrentLinkedQueue(initPlayerSocialBin.show_name_card_id_list)

    fun getShowNameCardIdList() =
        showNameCardIdList.toList()

    internal fun toBin(): PlayerSocialCompBin {
        return PlayerSocialCompBin(
            unlock_name_card_list = unlockNameCardList.toList(),
            birthday = birthday,
            signature = signature,
            name_card_id = nameCardId,
            recent_mp_player_uid_list = recentMpPlayerUidList.toList(),
            last_grant_birthday_benefit_year = lastGrantBirthdayBenefitYear,
            is_have_first_share = isHaveFirstShare,
            is_login_on_birthday = isLoginOnBirthday,
            friend_remark_name_map = friendRemarkNameMap,
            show_avatar_id_list = showAvatarIdList.toList(),
            is_show_avatar = isShowAvatar,
            show_name_card_id_list = showNameCardIdList.toList(),
        )
    }
}

fun Birthday.toProto() = BirthdayProto(
    month = month,
    day = day,
)
