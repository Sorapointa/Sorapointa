package org.sorapointa.game

import org.sorapointa.dataloader.common.ElementType
import org.sorapointa.dataloader.common.EntityIdType
import org.sorapointa.dataloader.common.FightProp.*
import org.sorapointa.dataloader.common.PlayerProp
import org.sorapointa.dataloader.def.*
import org.sorapointa.game.data.Position
import org.sorapointa.proto.*
import org.sorapointa.utils.*
import kotlin.contracts.contract

interface AvatarEntity : SceneEntity {

    val ownerPlayer: Player

    val avatar: AbstractAvatar

    val equipWeaponEntityId: Int?
        get() = avatar.equipWeapon?.let {
            ownerPlayer.getOrNextEntityId(EntityIdType.WEAPON, it.guid)
        }
}

abstract class AbstractAvatarEntity : SceneEntityBase(), AvatarEntity {

    internal abstract val avatarProto: AvatarProto
}

class AvatarEntityImpl(
    override val ownerPlayer: Player,
    override val avatar: AbstractAvatar,
) : AbstractAvatarEntity() {

    override val id: Int by lazy {
        ownerPlayer.getOrNextEntityId(EntityIdType.AVATAR, avatar.guid)
    }

    override val scene: Scene = ownerPlayer.scene

    override val position: Position
        get() = ownerPlayer.avatarComp.pbOnlyCurPos

    override val entityType = ProtEntityType.PROT_ENTITY_TYPE_AVATAR

    override val avatarProto: AvatarProto = AvatarProto(this)

    override val entityProto: SceneEntityProto<*> = avatarProto

    override fun toString(): String =
        "Avatar[guid: ${avatar.guid}, position: $position]"
}

@Suppress("NOTHING_TO_INLINE")
inline fun Map<Int, PropValue>.toFlattenPropMap(): Map<Int, Long> =
    map { it.key to it.value.val_ }.toMap()

internal class AvatarProto(
    override val entity: AvatarEntity
) : AbstractSceneEntityProto<AvatarEntity>() {

    private val avatar = entity.avatar
    private val excelData = avatar.excelData
    private val selectedDepot
        get() = avatar.depot.getSelectedDepot()

    private val protoExcelInfo by lazy {
        AvatarExcelInfo(
            prefab_path_hash = excelData.prefabPathHash.toLong(),
            prefab_path_remote_hash = excelData.prefabPathRemoteHash.toLong(),
            controller_path_hash = excelData.controllerPathHash.toLong(),
            controller_path_remote_hash = excelData.controllerPathRemoteHash.toLong(),
            combat_config_hash = excelData.combatConfigHash.toLong(),
        )
    }

    val protoPropMap
        get() = buildList {
            add(PlayerProp.PROP_LEVEL map avatar.level)
            if (avatar is FormalAvatar) {
                add(PlayerProp.PROP_EXP map avatar.exp)
            }
            add(PlayerProp.PROP_BREAK_LEVEL map avatar.promoteLevel)
            add(PlayerProp.PROP_SATIATION_VAL mapFloat avatar.satiationVal)
            add(PlayerProp.PROP_SATIATION_PENALTY_TIME mapFloat avatar.satiationPenaltyTime)
        }.toMap()

    private val protoSceneReliquaryInfoList
        get() = avatar.equipList.mapNotNull { if (it is ReliquaryItem) it.toSceneReliquaryInfoProto() else null }

    private val protoFightPropMap
        get() = buildList {
            add(FIGHT_PROP_BASE_HP map avatar.excelData.hpBase)
//            add(FIGHT_PROP_HP map avatar.curHp)
//            add(FIGHT_PROP_HP_PERCENT map entity.data.hpPercent)
            add(FIGHT_PROP_BASE_ATTACK map avatar.excelData.attackBase)
//            add(FIGHT_PROP_ATTACK map entity.data.attack)
//            add(FIGHT_PROP_ATTACK_PERCENT map entity.data.attackPercent)
            add(FIGHT_PROP_BASE_DEFENSE map avatar.excelData.defenseBase)
//            add(FIGHT_PROP_DEFENSE map entity.data.defense)
//            add(FIGHT_PROP_DEFENSE_PERCENT map entity.data.defensePercent)
//            add(FIGHT_PROP_BASE_SPEED map entity.data.baseSpeed)
//            add(FIGHT_PROP_SPEED_PERCENT map entity.data.speedPercent)
            add(FIGHT_PROP_CRITICAL map avatar.excelData.critical)
//            add(FIGHT_PROP_ANTI_CRITICAL map entity.data.antiCritical)
            add(FIGHT_PROP_CRITICAL_HURT map avatar.excelData.criticalHurt)
//            add(FIGHT_PROP_CHARGE_EFFICIENCY map entity.data.chargeEfficiency)
//            add(FIGHT_PROP_ADD_HURT map entity.data.addHurt)
//            add(FIGHT_PROP_SUB_HURT map entity.data.subHurt)
//            add(FIGHT_PROP_HEAL_ADD map entity.data.healAdd)
//            add(FIGHT_PROP_HEALED_ADD map entity.data.healedAdd)
//            add(FIGHT_PROP_ELEMENT_MASTERY map entity.data.elementMastery)
//            add(FIGHT_PROP_PHYSICAL_SUB_HURT map entity.data.physicalSubHurt)
//            add(FIGHT_PROP_PHYSICAL_ADD_HURT map entity.data.physicalAddHurt)
//            add(FIGHT_PROP_DEFENCE_IGNORE_RATIO map entity.data.defenceIgnoreRatio)
//            add(FIGHT_PROP_DEFENCE_IGNORE_DELTA map entity.data.defenceIgnoreDelta)
//            add(FIGHT_PROP_FIRE_ADD_HURT map entity.data.fireAddHurt)
//            add(FIGHT_PROP_ELEC_ADD_HURT map entity.data.electricAddHurt)
//            add(FIGHT_PROP_WATER_ADD_HURT map entity.data.waterAddHurt)
//            add(FIGHT_PROP_GRASS_ADD_HURT map entity.data.grassAddHurt)
//            add(FIGHT_PROP_WIND_ADD_HURT map entity.data.windAddHurt)
//            add(FIGHT_PROP_ROCK_ADD_HURT map entity.data.rockAddHurt)
//            add(FIGHT_PROP_ICE_ADD_HURT map entity.data.iceAddHurt)
//            add(FIGHT_PROP_HIT_HEAD_ADD_HURT map entity.data.hitHeadAddHurt)
//            add(FIGHT_PROP_FIRE_SUB_HURT map entity.data.fireSubHurt)
//            add(FIGHT_PROP_ELEC_SUB_HURT map entity.data.electricSubHurt)
//            add(FIGHT_PROP_WATER_SUB_HURT map entity.data.waterSubHurt)
//            add(FIGHT_PROP_GRASS_SUB_HURT map entity.data.grassSubHurt)
//            add(FIGHT_PROP_WIND_SUB_HURT map entity.data.windSubHurt)
//            add(FIGHT_PROP_ROCK_SUB_HURT map entity.data.rockSubHurt)
//            add(FIGHT_PROP_ICE_SUB_HURT map entity.data.iceSubHurt)
//            add(FIGHT_PROP_SKILL_CD_MINUS_RATIO map entity.data.skillCDMinusRatio)
//            add(FIGHT_PROP_SHIELD_COST_MINUS_RATIO map entity.data.shieldCostMinusRatio)
            if (selectedDepot.elementType != ElementType.None) {
                selectedDepot.maxEnergy?.let {
                    add(selectedDepot.elementType.maxEnergyProp map it)
                }
                add(selectedDepot.elementType.curEnergyProp map avatar.curElemEnergy)
            }
            // Must load maxHp before curHp to make sure curHp <= maxHp
            add(FIGHT_PROP_MAX_HP map entity.maxHp)
            add(FIGHT_PROP_CUR_HP map entity.curHp)
            add(FIGHT_PROP_CUR_ATTACK map entity.curAttack)
            add(FIGHT_PROP_CUR_DEFENSE map entity.curDefense)
            FIGHT_PROP_CUR_SPEED map entity.curSpeed
        }.toMap().filter { it.value != 0f }

    override val fightPropPairList: List<FightPropPair>
        get() = protoFightPropMap.map { it.key fightProp it.value } // crazy hoyo

    fun toAvatarInfoProto(): AvatarInfo = AvatarInfo(
        avatar_id = avatar.avatarId,
        guid = avatar.guid,
        prop_map = protoPropMap,
        life_state = avatar.lifeState,
        equip_guid_list = avatar.equipGuidList,
        talent_id_list = selectedDepot.talentList,
        fight_prop_map = protoFightPropMap,
        skill_map = selectedDepot.getProtoSkillMap(),
        skill_depot_id = avatar.depot.selectedDepotId,
        // TODO: Fetter system
        // Official server packet doesn't have `coreProudSkillLevel` field
        core_proud_skill_level = selectedDepot.coreProudSkillLevel,
        inherent_proud_skill_list = selectedDepot.inherentProudSkillList,
        skill_level_map = selectedDepot.getSkillLevelMap(),
        // TODO: Proud skill extra level map
        // TODO: isFocus is whether one of teammate of selected team
        avatar_type = avatar.avatarType.value,
        // TODO: Team resonance
        wearing_flycloak_id = avatar.flyCloakId.value,
        // Still don't know what is `equip_affix_list` in this packet
        // It seems related to weapon passive skill and cd time
        // 比如西风和祭礼系列武器的被动冷却时间 - 持久化数据库存储，就像 protoSkillMap
        born_time = avatar.bornTime,
        // TODO: `pending_promote_reward_list`
        costume_id = avatar.costumeId,
        excel_info = protoExcelInfo,
    )

    override fun SceneEntityInfo.toProto(): SceneEntityInfo {
        val avatarProto = getSceneAvatarInfo()
        return copy(avatar = avatarProto)
    }

    fun getSceneAvatarInfo(): SceneAvatarInfo {
        val weapon = if (avatar.equipWeapon != null && entity.equipWeaponEntityId != null) {
            avatar.equipWeapon!!.toSceneWeaponInfoProto(entity.equipWeaponEntityId!!)
        } else null

        return SceneAvatarInfo(
            uid = entity.ownerPlayer.uid,
            avatar_id = avatar.avatarId,
            guid = avatar.guid,
            peer_id = entity.ownerPlayer.peerId,
            equip_id_list = avatar.equipIdList,
            skill_depot_id = avatar.depot.selectedDepotId,
            talent_id_list = selectedDepot.talentList,
            weapon = weapon,
            reliquary_list = protoSceneReliquaryInfoList,
            core_proud_skill_level = selectedDepot.coreProudSkillLevel,
            inherent_proud_skill_list = selectedDepot.inherentProudSkillList,
            skill_level_map = selectedDepot.getSkillLevelMap(),
            // TODO: Proud skill extra level map
            // TODO: Server Buff List
            // TODO: Team resonance
            wearing_flycloak_id = avatar.flyCloakId.value,
            born_time = avatar.bornTime,
            // TODO: `pending_promote_reward_list`
            costume_id = avatar.costumeId,
            // curVehicleInfo
            excel_info = protoExcelInfo,
            // animHash
        )
    }

    fun getAbilityControlBlock(): AbilityControlBlock {
        // TODO: hardcode
        if (avatar.avatarId == 10000005) {
            val abilities = listOf(
                "Avatar_PlayerBoy_NormalAttack_DamageHandler",
                "Avatar_Player_FlyingBomber",
                "Avatar_Player_CamCtrl",
                "Avatar_PlayerBoy_FallingAnthem",
                "GrapplingHookSkill_Ability",
                "Avatar_DefaultAbility_VisionReplaceDieInvincible",
                "Avatar_DefaultAbility_AvartarInShaderChange",
                "Avatar_SprintBS_Invincible",
                "Avatar_Freeze_Duration_Reducer",
                "Avatar_Attack_ReviveEnergy",
                "Avatar_Component_Initializer",
                "Avatar_HDMesh_Controller",
                "Avatar_Trampoline_Jump_Controller",
                "Avatar_PlayerBoy_ExtraAttack_Common",
                "Avatar_FallAnthem_Achievement_Listener"
            )
            return AbilityControlBlock(
                ability_embryo_list = abilities.map {
                    AbilityEmbryo(
                        ability_id = abilities.indexOf(it),
                        ability_name_hash = it.hashCode(),
                        ability_override_name_hash = 1178079449,
                    )
                },
            )
        } else if (avatar.avatarId == 10000007) {
            val abilities = listOf(
                "Avatar_PlayerGirl_NormalAttack_DamageHandler",
                "Avatar_Player_FlyingBomber",
                "Avatar_Player_CamCtrl",
                "Avatar_PlayerGirl_FallingAnthem",
                "GrapplingHookSkill_Ability",
                "Avatar_DefaultAbility_VisionReplaceDieInvincible",
                "Avatar_DefaultAbility_AvartarInShaderChange",
                "Avatar_SprintBS_Invincible",
                "Avatar_Freeze_Duration_Reducer",
                "Avatar_Attack_ReviveEnergy",
                "Avatar_Component_Initializer",
                "Avatar_HDMesh_Controller",
                "Avatar_Trampoline_Jump_Controller",
                "Avatar_PlayerGirl_ExtraAttack_Common",
                "Avatar_FallAnthem_Achievement_Listener",
            )
            return AbilityControlBlock(
                ability_embryo_list = abilities.map {
                    AbilityEmbryo(
                        ability_id = abilities.indexOf(it),
                        ability_name_hash = it.hashCode(),
                        ability_override_name_hash = 1178079449,
                    )
                },
            )
        }
        return AbilityControlBlock()
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun AvatarEntity.impl(): AbstractAvatarEntity {
    contract { returns() implies (this@impl is AbstractAvatarEntity) }
    check(this is AbstractAvatarEntity) {
        "A Avatar instance is not instance of AbstractAvatar. Your instance: ${this::class.qualifiedOrSimple}"
    }
    return this
}
