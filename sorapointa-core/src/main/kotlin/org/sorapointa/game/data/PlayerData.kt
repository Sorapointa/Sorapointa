package org.sorapointa.game.data

import com.google.protobuf.ProtocolMessageEnum
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.sorapointa.data.provider.sql.SQLDatabaseMap
import org.sorapointa.data.provider.sql.SQLDatabaseSet
import org.sorapointa.data.provider.sql.SetTable
import org.sorapointa.data.provider.sql.jsonb
import org.sorapointa.dataloader.common.FlyCloakId
import org.sorapointa.dataloader.common.OpenState
import org.sorapointa.dataloader.common.PlayerProp
import org.sorapointa.dataloader.common.PlayerProp.*
import org.sorapointa.game.GuidEntity
import org.sorapointa.game.Player
import org.sorapointa.game.impl
import org.sorapointa.proto.MpSettingTypeOuterClass.MpSettingType
import org.sorapointa.proto.avatarTeam
import org.sorapointa.proto.birthday
import org.sorapointa.proto.profilePicture
import org.sorapointa.server.network.PlayerPropNotifyPacket
import org.sorapointa.utils.*
import java.util.*

internal object PlayerDataTable : IdTable<Int>("player_data") {
    const val LOCALE_LENGTH_LIMIT = 20

    override val id: Column<EntityID<Int>> = integer("user_id")
        .entityId()
    val guidCounter: Column<Long> = long("guid_counter")
        .default(0L)
    val locale: Column<String?> = varchar("locale", LOCALE_LENGTH_LIMIT)
        .nullable()
    val nickName: Column<String> = varchar("nick_name", NICK_NAME_LENGTH_LIMIT)
        .default("Sorapointa Player")
    val signature: Column<String> = varchar("signature", 50)
        .default("")
    val position: Column<Position> = jsonb<Position>("position")
        .default(START_POSITION)
    val rotation: Column<Position> = jsonb<Position>("rotation")
        .default(Position(0))
    val birthday: Column<PlayerBirthday> = jsonb<PlayerBirthday>("birthday")
        .default(PlayerBirthday(1, 1))
    val profilePicture: Column<ProfilePictureData> = jsonb<ProfilePictureData>("profile_picture")
        .default(ProfilePictureData(1))
    val nameCardId: Column<Int> = integer("name_card_id")
        .default(DEFAULT_NAME_CARD)
    val selectedTeamId: Column<Int> = integer("selected_team_id")
        .default(1)
    val selectedAvatarGuid: Column<Long> = long("selected_avatar_guid")
        .default(1L)
    val compoundAvatarTeam: Column<CompoundAvatarTeamData> = jsonb<CompoundAvatarTeamData>("compound_avatar_team")
        .default(CompoundAvatarTeamData())
    val lastActiveTime: Column<Instant> = timestamp("lastActiveTime")

    val sceneId: Column<Int> = integer("scene_id")
        .default(BIG_WORLD_SCENE_ID)

    val maxSpringVolume: Column<Int> = integer("max_spring_volume")
        .default(0)
    val curSpringVolume: Column<Int> = integer("cur_spring_volume")
        .default(0)
    val isSpringAutoUse: Column<Boolean> = bool("is_spring_auto_use")
        .default(true)
    val springAutoUsePercent: Column<Int> = integer("spring_auto_use_percent")
        .default(50)
    val isFlyable: Column<Boolean> = bool("is_flyable")
        .default(true)

    // isWeatherLocked, isGameTimeLocked
    val isTransferable: Column<Boolean> = bool("is_transferable")
        .default(true)
    val maxStamina: Column<Int> = integer("max_stamina")
        .default(MAX_STAMINA)
    val curPersistStamina: Column<Int> = integer("cur_persist_stamina")
        .default(MAX_STAMINA)
    val curTemporaryStamina: Column<Int> = integer("cur_termproray_stamina")
        .default(0)
    val playerLevel: Column<Int> = integer("player_level")
        .default(1)
    val exp: Column<Int> = integer("exp")
        .default(0)
    val primoGem: Column<Int> = integer("primo_gem")
        .default(0)
    val mora: Column<Int> = integer("mora")
        .default(0)
    val mpSettingType: Column<MpSettingType> = enumeration<MpSettingType>("mp_setting_type")
        .default(MpSettingType.MP_SETTING_TYPE_ENTER_AFTER_APPLY)
    val isMpModeAvailable: Column<Boolean> = bool("is_mp_mode_available")
        .default(false)
    val worldLevel: Column<Int> = integer("world_level")
        .default(0)
    val playerResin: Column<Int> = integer("player_resin")
        .default(MAX_RESIN)
    val genesisCrystal: Column<Int> = integer("genesis_crystal")
        .default(0)
    val legendaryStoryKey: Column<Int> = integer("legendary_story_key")
        .default(0)
    val isHasFirstShare: Column<Boolean> = bool("is_has_first_share")
        .default(false)
    val playerForgePoint: Column<Int> = integer("player_forge_point")
        .default(0)

    // curClimateMeter, curClimateType, curClimateAreaId, curClimateAreaClimateType, worldLevelLimit
    val worldLevelAdjustCD: Column<Int> = integer("world_level_adjust_cd")
        .default(0)
    val legendaryDailyTaskSum: Column<Int> = integer("legendary_daily_task_sum")
        .default(0)
    val homeCoin: Column<Int> = integer("home_coin")
        .default(0)
    // isAutoUnlockSpecificEquip

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

internal object PlayerFriendRelationTable : Table("player_friend_relation") {

    val uid1: Column<EntityID<Int>> =
        reference("uid1", PlayerDataTable.id, onDelete = ReferenceOption.CASCADE)
    val uid2: Column<EntityID<Int>> =
        reference("uid2", PlayerDataTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey: PrimaryKey = PrimaryKey(uid1, uid2)

    fun findPlayerFriendList(uid: Int) = select {
        (uid1 eq uid) or (uid2 eq uid)
    }.map {
        val uid1 = it[uid1].value
        if (uid1 != uid) uid1 else it[uid2].value
    }

    fun isFriendRelation(id1: Int, id2: Int): Boolean {
        if (id1 == id2) return true
        return (select { (uid1 eq id1) and (uid2 eq id2) }.fetchSize ?: 0) >= 1
    }

}

internal object OpenStateSetTable : SetTable<Int, OpenState>("player_open_state_data") {

    override val id: Column<EntityID<Int>> =
        reference("user_id", PlayerDataTable, onDelete = ReferenceOption.CASCADE)

    override val value: Column<OpenState> = enumeration("opened_state")

    override val primaryKey: PrimaryKey = PrimaryKey(id, value)
}

internal object FlyCloakSetTable : SetTable<Int, FlyCloakId>("player_fly_cloak_list") {

    override val id: Column<EntityID<Int>> =
        reference("user_id", PlayerDataTable, onDelete = ReferenceOption.CASCADE)

    override val value: Column<FlyCloakId> = enumeration("fly_cloak")

    override val primaryKey: PrimaryKey = PrimaryKey(id, value)
}

internal object CostumeSetTable : SetTable<Int, Int>("player_costume_list") {

    override val id: Column<EntityID<Int>> =
        reference("user_id", PlayerDataTable, onDelete = ReferenceOption.CASCADE)

    override val value: Column<Int> = integer("costume_id")

    override val primaryKey: PrimaryKey = PrimaryKey(id, value)
}

internal object NameCardSetTable : SetTable<Int, Int>("player_name_card_list") {

    override val id: Column<EntityID<Int>> =
        reference("user_id", PlayerDataTable, onDelete = ReferenceOption.CASCADE)

    override val value: Column<Int> = integer("name_card_id")

    override val primaryKey: PrimaryKey = PrimaryKey(id, value)
}

interface PlayerData : GuidEntity {

    val uid: Int

    var guidCounter: Long
    var locale: Locale?
    var nickName: String
    var signature: String
    var position: Position
    var rotation: Position
    var birthday: PlayerBirthday
    var profilePicture: ProfilePictureData
    var nameCardId: Int
    var selectedTeamId: Int
    var selectedAvatarGuid: Long
    var compoundAvatarTeam: CompoundAvatarTeamData
    var lastActiveTime: Instant

    var sceneId: Int

    var maxSpringVolume: Int
    var curSpringVolume: Int
    var isSpringAutoUse: Boolean
    var springAutoUsePercent: Int
    var isFlyable: Boolean

    var isTransferable: Boolean
    var maxStamina: Int
    var curPersistStamina: Int
    var curTemporaryStamina: Int
    var playerLevel: Int
    var exp: Int
    var primoGem: Int
    var mora: Int
    var mpSettingType: MpSettingType
    var isMpModeAvailable: Boolean
    var worldLevel: Int
    var playerResin: Int
    var genesisCrystal: Int
    var legendaryStoryKey: Int
    var isHasFirstShare: Boolean
    var playerForgePoint: Int

    var worldLevelAdjustCD: Int
    var legendaryDailyTaskSum: Int
    var homeCoin: Int

    val inventory: MutableMap<Long, ItemData>
    val openStateSet: MutableSet<OpenState>
    val flyCloakSet: MutableSet<FlyCloakId>
    val nameCardSet: MutableSet<Int>
    val costumeSet: MutableSet<Int>

    val friendList: List<Int>

    val daysSinceLastLogin
        get() = (now() - lastActiveTime).inWholeDays

    val isFirstLoginToday
        get() = lastActiveTime < todayStartTime

    override fun getNextGuid(): Long {
        val nextGuid = ++guidCounter
        return (uid shl 32) + nextGuid
    }

}

@Suppress("MemberVisibilityCanBePrivate")
class PlayerDataImpl(id: EntityID<Int>) : Entity<Int>(id), PlayerData {

    var player: Player? = null

    companion object : EntityClass<Int, PlayerDataImpl>(PlayerDataTable) {

        internal fun create(id: Int, nickName: String, pickInitAvatarId: Int): PlayerData =
            PlayerDataImpl.new(id) {
                this.nickName = nickName
                this.profilePicture = ProfilePictureData(pickInitAvatarId)
                this.lastActiveTime = now()
            }.apply {
                openStateSet.add(OpenState.OPEN_STATE_NONE)
                flyCloakSet.add(FlyCloakId.GLIDER)
                nameCardSet.add(DEFAULT_NAME_CARD)
            }
    }

    override val uid = id.value

    override var guidCounter by PlayerDataTable.guidCounter
    override var locale: Locale? by PlayerDataTable.locale.transform(
        toColumn = { it?.toLanguageTag() },
        toReal = { it?.let { Locale.forLanguageTag(it) } }
    )
    override var nickName by PlayerDataTable.nickName
    override var signature by PlayerDataTable.signature
    override var position by PlayerDataTable.position
    override var rotation by PlayerDataTable.rotation
    override var birthday by PlayerDataTable.birthday
    override var profilePicture by PlayerDataTable.profilePicture
    override var nameCardId by PlayerDataTable.nameCardId
    override var selectedTeamId by PlayerDataTable.selectedTeamId
    override var selectedAvatarGuid by PlayerDataTable.selectedAvatarGuid
    override var compoundAvatarTeam by PlayerDataTable.compoundAvatarTeam
    override var lastActiveTime by PlayerDataTable.lastActiveTime

    override var sceneId by PlayerDataTable.sceneId

    override var maxSpringVolume by PlayerDataTable.maxSpringVolume playerProp PROP_MAX_SPRING_VOLUME
    override var curSpringVolume by PlayerDataTable.curSpringVolume playerProp PROP_CUR_SPRING_VOLUME
    override var isSpringAutoUse by PlayerDataTable.isSpringAutoUse playerProp PROP_IS_SPRING_AUTO_USE
    override var springAutoUsePercent by PlayerDataTable.springAutoUsePercent playerProp PROP_SPRING_AUTO_USE_PERCENT
    override var isFlyable by PlayerDataTable.isFlyable playerProp PROP_IS_FLYABLE
    override var isTransferable by PlayerDataTable.isTransferable playerProp PROP_IS_TRANSFERABLE
    override var maxStamina by PlayerDataTable.maxStamina playerProp PROP_MAX_STAMINA
    override var curPersistStamina by PlayerDataTable.curPersistStamina playerProp PROP_CUR_PERSIST_STAMINA
    override var curTemporaryStamina by PlayerDataTable.curTemporaryStamina playerProp PROP_CUR_TEMPORARY_STAMINA
    override var playerLevel by PlayerDataTable.playerLevel playerProp PROP_PLAYER_LEVEL
    override var exp by PlayerDataTable.exp playerProp PROP_EXP
    override var primoGem by PlayerDataTable.primoGem playerProp PROP_PLAYER_HCOIN
    override var mora by PlayerDataTable.mora playerProp PROP_PLAYER_SCOIN
    override var mpSettingType by PlayerDataTable.mpSettingType playerProp PROP_PLAYER_MP_SETTING_TYPE
    override var isMpModeAvailable by PlayerDataTable.isMpModeAvailable playerProp PROP_IS_MP_MODE_AVAILABLE
    override var worldLevel by PlayerDataTable.worldLevel playerProp PROP_PLAYER_WORLD_LEVEL
    override var playerResin by PlayerDataTable.playerResin playerProp PROP_PLAYER_RESIN
    override var genesisCrystal by PlayerDataTable.genesisCrystal playerProp PROP_PLAYER_MCOIN
    override var legendaryStoryKey by PlayerDataTable.legendaryStoryKey playerProp PROP_PLAYER_LEGENDARY_KEY
    override var isHasFirstShare by PlayerDataTable.isHasFirstShare playerProp PROP_IS_HAS_FIRST_SHARE
    override var playerForgePoint by PlayerDataTable.playerForgePoint playerProp PROP_PLAYER_FORGE_POINT
    override var worldLevelAdjustCD by PlayerDataTable.worldLevelAdjustCD playerProp PROP_PLAYER_WORLD_LEVEL_ADJUST_CD
    override var legendaryDailyTaskSum
        by PlayerDataTable.legendaryDailyTaskSum playerProp PROP_PLAYER_LEGENDARY_DAILY_TASK_NUM
    override var homeCoin by PlayerDataTable.homeCoin playerProp PROP_PLAYER_HOME_COIN

    override val inventory = SQLDatabaseMap(id, InventoryTable)
    override val openStateSet = SQLDatabaseSet(id, OpenStateSetTable)
    override val flyCloakSet = SQLDatabaseSet(id, FlyCloakSetTable)
    override val nameCardSet = SQLDatabaseSet(id, NameCardSetTable)
    override val costumeSet = SQLDatabaseSet(id, CostumeSetTable)
    override val friendList
        get() = PlayerFriendRelationTable.findPlayerFriendList(uid)

    override fun getNextGuid(): Long {
        val nextGuid = ++guidCounter
        return (id.value.toLong() shl 32) + nextGuid
    }

    private infix fun <T> Column<T>.playerProp(playerProp: PlayerProp?) =
        SQLPropDelegate(this@PlayerDataImpl, this, playerProp) { prop, value ->
            val player = player ?: return@SQLPropDelegate
            val converted = when (value) {
                is Number -> value.toLong()
                is Boolean -> value.toInt().toLong()
                is ProtocolMessageEnum -> value.number.toLong()
                else -> error("Could not convert $prop value $value to long")
            }
            player.impl().sendPacket(
                PlayerPropNotifyPacket(
                    player = player,
                    propMap = mapOf(prop map converted)
                )
            )
        }
}

@Suppress("NOTHING_TO_INLINE")
inline fun CompoundAvatarTeamData(initAvatarGuid: Long = 0) =
    CompoundAvatarTeamData(
        teamMap = hashMapOf(
            1 to CompoundAvatarTeamData.AvatarTeamData(
                teamName = "Default",
                avatarGuidList = listOf(initAvatarGuid)
            )
        )
    )

@Serializable
data class CompoundAvatarTeamData(
    val teamMap: Map<Int, AvatarTeamData>,
) {

    val protoTeamMap by lazy {
        teamMap.map { it.key to it.value.toProto() }.toMap()
    }

    @Serializable
    data class AvatarTeamData(
        val teamName: String,
        val avatarGuidList: List<Long>
    ) {

        fun toProto() =
            avatarTeam {
                teamName = this@AvatarTeamData.teamName
                avatarGuidList.addAll(this@AvatarTeamData.avatarGuidList)
            }
    }
}

@Serializable
data class ProfilePictureData(
    val avatarId: Int,
    val costumeId: Int? = null
) {

    fun toProto() =
        profilePicture {
            avatarId = this@ProfilePictureData.avatarId
            costumeId = this@ProfilePictureData.costumeId ?: 0
        }
}

@Serializable
data class PlayerBirthday(
    val month: Int,
    val day: Int
) {

    fun toProto() =
        birthday {
            month = this@PlayerBirthday.month
            day = this@PlayerBirthday.day
        }
}
