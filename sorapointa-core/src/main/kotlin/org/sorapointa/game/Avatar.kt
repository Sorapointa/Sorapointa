package org.sorapointa.game

import org.sorapointa.dataloader.common.AvatarType
import org.sorapointa.dataloader.common.ElementType
import org.sorapointa.dataloader.common.EntityIdType
import org.sorapointa.dataloader.common.FightProp.*
import org.sorapointa.dataloader.common.PlayerProp
import org.sorapointa.dataloader.def.*
import org.sorapointa.game.data.AvatarData
import org.sorapointa.game.data.ItemData
import org.sorapointa.game.data.Position
import org.sorapointa.proto.*
import org.sorapointa.proto.AvatarInfoOuterClass.AvatarInfo
import org.sorapointa.proto.PropValueOuterClass.PropValue
import org.sorapointa.utils.*
import kotlin.contracts.contract

interface Avatar : SceneEntity {

    val ownerPlayer: Player
    val data: AvatarData
    val avatarType: AvatarType

    val guid: Long

    val avatarExcelData: AvatarExcelData

    val avatarSkillDepotData: AvatarSkillDepotData

    val normalAttack: AvatarSkillData

    val elementSkill: AvatarSkillData

    val energySkill: AvatarSkillData

    val elementType: ElementType

    val maxEnergy: Int

    val inherentProudSkillList: List<Int>

    val talentList: List<Int>

    val equipFlower: ItemData.Equip.Reliquary?
        get() = ownerPlayer.inventory.findItem(data.equipFlower).safeCast()
    val equipPlume: ItemData.Equip.Reliquary?
        get() = ownerPlayer.inventory.findItem(data.equipPlume).safeCast()
    val equipSands: ItemData.Equip.Reliquary?
        get() = ownerPlayer.inventory.findItem(data.equipSands).safeCast()
    val equipGoblet: ItemData.Equip.Reliquary?
        get() = ownerPlayer.inventory.findItem(data.equipGoblet).safeCast()
    val equipCirclet: ItemData.Equip.Reliquary?
        get() = ownerPlayer.inventory.findItem(data.equipCirclet).safeCast()
    val equipWeapon: ItemData.Equip.Weapon?
        get() = ownerPlayer.inventory.findItem(data.equipWeapon).safeCast()

    val equipWeaponEntityId: Int?
        get() = data.equipWeapon?.let { ownerPlayer.getOrNextEntityId(EntityIdType.WEAPON, it) }

    private fun Inventory.findItem(guid: Long?): ItemData? =
        guid?.let { findItem(guid) }
}

abstract class AbstractAvatar : SceneEntityBase(), Avatar {

    internal abstract val avatarProto: AvatarProto

    internal abstract fun initNewAvatar()
}

class AvatarImpl(
    override val ownerPlayer: Player,
    override val data: AvatarData,
    override val avatarType: AvatarType = AvatarType.FORMAL,
) : AbstractAvatar() {

    override val guid = data.guid

    override val id: Int by lazy {
        ownerPlayer.getOrNextEntityId(EntityIdType.AVATAR, guid)
    }

    override val scene: Scene = ownerPlayer.scene

    override val position: Position = ownerPlayer.data.position

    override val entityType = ProtEntityTypeOuterClass.ProtEntityType.PROT_ENTITY_TYPE_AVATAR

    override val avatarProto: AvatarProto = AvatarProtoImpl(this)

    override val entityProto: SceneEntityProto<*> = avatarProto

    override val avatarExcelData: AvatarExcelData by lazy {
        getAvatarExcelData(data.avatarId) ?: error("Could not find avatarId: ${data.avatarId} excel data")
    }

    override val avatarSkillDepotData: AvatarSkillDepotData by lazy {
        avatarExcelData.skillDepotData ?: error("Could not find skillDepotId: ${avatarExcelData.skillDepotId}")
    }

    override val normalAttack by lazy {
        avatarSkillDepotData.normalAttack
    }

    override val elementSkill by lazy {
        avatarSkillDepotData.elementSkill
    }

    override val energySkill by lazy {
        avatarSkillDepotData.energySkill
    }

    override val elementType: ElementType by lazy {
        avatarSkillDepotData.energySkill.costElemType
    }

    override val maxEnergy by lazy {
        avatarSkillDepotData.energySkill.costElemVal.toInt()
    }

    override val maxHp
        get() =
            calculateAvatarStats(data.baseHp, data.hp, data.hpPercent).also { max ->
                if (data.currentHP > max) {
                    data.currentHP = max
                }
            }

    override val curAttack
        get() =
            calculateAvatarStats(data.baseAttack, data.attack, data.attackPercent)

    override val curDefense
        get() =
            calculateAvatarStats(data.baseDefense, data.defense, data.defensePercent)

    override val curSpeed
        get() =
            calculateAvatarStats(data.baseSpeed, 0f, data.speedPercent)

    // TODO: Cache Optimize
    override val inherentProudSkillList
        get() = avatarSkillDepotData
            .inherentProudSkillOpens
            .asSequence()
            .filter { data.promoteLevel >= it.needAvatarPromoteLevel }
            .map { it.proudSkillGroupId }
            .toList()

    /**
     * C6, 10000007 -> 71, 72, 73, 74, 75, 76
     */
    override val talentList
        get() = List(data.constellationLevel) {
            ((data.avatarId - 10000000) * 10) + (it + 1)
        }

    override fun initNewAvatar() {
        // TODO
        with(ownerPlayer.inventory) {
            putItem(createWeaponItem(avatarExcelData.initialWeapon))
        }
    }

    override fun toString(): String =
        "Avatar[guid: $guid, position: $position]"
}

internal interface AvatarProto : SceneEntityProto<Avatar> {

    val protoPropMap: Map<Int, PropValue>

    fun toAvatarInfoProto(): AvatarInfo
}

@Suppress("NOTHING_TO_INLINE")
inline fun Map<Int, PropValue>.toFlattenPropMap() =
    map { it.key to it.value.`val` }.toMap()

internal class AvatarProtoImpl(
    override val entity: Avatar
) : AbstractSceneEntityProto<Avatar>(), AvatarProto {

    private val protoExcelInfo = avatarExcelInfo {
        prefabPathHash = entity.avatarExcelData.prefabPathHash
        prefabPathRemoteHash = entity.avatarExcelData.prefabPathRemoteHash
        controllerPathHash = entity.avatarExcelData.controllerPathHash
        controllerPathRemoteHash = entity.avatarExcelData.controllerPathRemoteHash
        combatConfigHash = entity.avatarExcelData.combatConfigHash
    }

    override val protoPropMap
        get() = mapOf(
            PlayerProp.PROP_LEVEL map entity.data.level,
            PlayerProp.PROP_EXP map entity.data.exp,
            PlayerProp.PROP_BREAK_LEVEL map entity.data.promoteLevel,
            PlayerProp.PROP_SATIATION_VAL map entity.data.satiationVal,
            PlayerProp.PROP_SATIATION_PENALTY_TIME map entity.data.satiationPenaltyTime,
        )

    private val protoEquipGuidList
        get() = listOfNotNull(
            entity.data.equipFlower,
            entity.data.equipPlume,
            entity.data.equipSands,
            entity.data.equipGoblet,
            entity.data.equipCirclet,
            entity.data.equipWeapon,
        )

    private val equipList
        get() = listOfNotNull(
            entity.equipFlower,
            entity.equipPlume,
            entity.equipSands,
            entity.equipGoblet,
            entity.equipCirclet,
            entity.equipWeapon,
        )

    private val protoEquipIdList
        get() = equipList.map { it.itemId }

    private val protoSceneReliquaryInfoList
        get() = equipList.mapNotNull { if (it is ItemData.Equip.Reliquary) it.toSceneReliquaryInfoProto() else null }

    private val protoFightPropMap
        get() = mapOf(
            FIGHT_PROP_BASE_HP map entity.data.baseHp,
            FIGHT_PROP_HP map entity.data.hp,
            FIGHT_PROP_HP_PERCENT map entity.data.hpPercent,
            FIGHT_PROP_BASE_ATTACK map entity.data.baseAttack,
            FIGHT_PROP_ATTACK map entity.data.attack,
            FIGHT_PROP_ATTACK_PERCENT map entity.data.attackPercent,
            FIGHT_PROP_BASE_DEFENSE map entity.data.baseDefense,
            FIGHT_PROP_DEFENSE map entity.data.defense,
            FIGHT_PROP_DEFENSE_PERCENT map entity.data.defensePercent,
            FIGHT_PROP_BASE_SPEED map entity.data.baseSpeed,
            FIGHT_PROP_SPEED_PERCENT map entity.data.speedPercent,
            FIGHT_PROP_CRITICAL map entity.data.critical,
            FIGHT_PROP_ANTI_CRITICAL map entity.data.antiCritical,
            FIGHT_PROP_CRITICAL_HURT map entity.data.criticalHurt,
            FIGHT_PROP_CHARGE_EFFICIENCY map entity.data.chargeEfficiency,
            FIGHT_PROP_ADD_HURT map entity.data.addHurt,
            FIGHT_PROP_SUB_HURT map entity.data.subHurt,
            FIGHT_PROP_HEAL_ADD map entity.data.healAdd,
            FIGHT_PROP_HEALED_ADD map entity.data.healedAdd,
            FIGHT_PROP_ELEMENT_MASTERY map entity.data.elementMastery,
            FIGHT_PROP_PHYSICAL_SUB_HURT map entity.data.physicalSubHurt,
            FIGHT_PROP_PHYSICAL_ADD_HURT map entity.data.physicalAddHurt,
            FIGHT_PROP_DEFENCE_IGNORE_RATIO map entity.data.defenceIgnoreRatio,
            FIGHT_PROP_DEFENCE_IGNORE_DELTA map entity.data.defenceIgnoreDelta,
            FIGHT_PROP_FIRE_ADD_HURT map entity.data.fireAddHurt,
            FIGHT_PROP_ELEC_ADD_HURT map entity.data.electricAddHurt,
            FIGHT_PROP_WATER_ADD_HURT map entity.data.waterAddHurt,
            FIGHT_PROP_GRASS_ADD_HURT map entity.data.grassAddHurt,
            FIGHT_PROP_WIND_ADD_HURT map entity.data.windAddHurt,
            FIGHT_PROP_ROCK_ADD_HURT map entity.data.rockAddHurt,
            FIGHT_PROP_ICE_ADD_HURT map entity.data.iceAddHurt,
            FIGHT_PROP_HIT_HEAD_ADD_HURT map entity.data.hitHeadAddHurt,
            FIGHT_PROP_FIRE_SUB_HURT map entity.data.fireSubHurt,
            FIGHT_PROP_ELEC_SUB_HURT map entity.data.electricSubHurt,
            FIGHT_PROP_WATER_SUB_HURT map entity.data.waterSubHurt,
            FIGHT_PROP_GRASS_SUB_HURT map entity.data.grassSubHurt,
            FIGHT_PROP_WIND_SUB_HURT map entity.data.windSubHurt,
            FIGHT_PROP_ROCK_SUB_HURT map entity.data.rockSubHurt,
            FIGHT_PROP_ICE_SUB_HURT map entity.data.iceSubHurt,
            FIGHT_PROP_SKILL_CD_MINUS_RATIO map entity.data.skillCDMinusRatio,
            FIGHT_PROP_SHIELD_COST_MINUS_RATIO map entity.data.shieldCostMinusRatio,
            entity.elementType.maxEnergyProp map entity.maxEnergy,
            entity.elementType.curEnergyProp map entity.data.currentEnergy,
            // Must load maxHp before curHp to make sure curHp <= maxHp
            FIGHT_PROP_MAX_HP map entity.maxHp,
            FIGHT_PROP_CUR_HP map entity.data.currentHP,
            FIGHT_PROP_CUR_ATTACK map entity.curAttack,
            FIGHT_PROP_CUR_DEFENSE map entity.curDefense,
            FIGHT_PROP_CUR_SPEED map entity.curSpeed
        ).filter { it.value != 0f }

    override val fightPropPairList: List<FightPropPairOuterClass.FightPropPair>
        get() = protoFightPropMap.map { it.key fightProp it.value } // crazy hoyo

    private val protoSkillMap
        get() = mapOf(
            // TODO: Extra count from constellation, CD time store
            entity.elementSkill.id to avatarSkillInfo {
                maxChargeCount = entity.elementSkill.maxChargeNum
            },
            entity.energySkill.id to avatarSkillInfo {
                maxChargeCount = entity.energySkill.maxChargeNum
            }
        )

    private val protoSkillLevelMap
        get() = mapOf(
            entity.normalAttack.id to entity.data.normalAttackLevel,
            entity.elementSkill.id to entity.data.elementSkillLevel,
            entity.energySkill.id to entity.data.energySkillLevel
        )

    override fun toAvatarInfoProto(): AvatarInfo =
        avatarInfo {
            avatarId = entity.data.avatarId
            guid = entity.data.guid
            propMap.putAll(protoPropMap)
            lifeState = entity.data.lifeState.value
            equipGuidList.addAll(protoEquipGuidList)
            talentIdList.addAll(entity.talentList)
            fightPropMap.putAll(protoFightPropMap)
            skillMap.putAll(protoSkillMap)
            skillDepotId = entity.data.skillDepotId
            // TODO: Fetter system
            // Official server packet doesn't have `coreProudSkillLevel` field
            coreProudSkillLevel = entity.data.constellationLevel
            inherentProudSkillList.addAll(entity.inherentProudSkillList)
            skillLevelMap.putAll(protoSkillLevelMap)
            // TODO: Proud skill extra level map
            // TODO: isFocus is whether one of teammate of selected team
            avatarType = entity.avatarType.value
            // TODO: Team resonance
            wearingFlycloakId = entity.data.wearingFlyCloakId.id
            // Still don't know what is `equip_affix_list` in this packet
            // It seems related to weapon passive skill and cd time
            // 比如西风和祭礼系列武器的被动冷却时间 - 持久化数据库存储，就像 protoSkillMap
            bornTime = entity.data.bornTime.epochSeconds.toInt()
            // TODO: `pending_promote_reward_list`
            entity.data.costumeId?.let { costumeId = it }
            excelInfo = protoExcelInfo
        }

    override fun SceneEntityInfoKt.Dsl.toProto() {
        avatar = sceneAvatarInfo {
            uid = entity.ownerPlayer.uid
            avatarId = entity.data.avatarId
            guid = entity.data.guid
            peerId = entity.ownerPlayer.peerId
            equipIdList.addAll(protoEquipIdList)
            skillDepotId = entity.data.skillDepotId
            talentIdList.addAll(entity.talentList)
            entity.equipWeapon?.also { weaponData ->
                entity.equipWeaponEntityId?.also { entityId ->
                    weapon = weaponData.toSceneWeaponInfoProto(entityId)
                }
            }
            reliquaryList.addAll(protoSceneReliquaryInfoList)
            coreProudSkillLevel = entity.data.constellationLevel
            inherentProudSkillList.addAll(entity.inherentProudSkillList)
            skillLevelMap.putAll(protoSkillLevelMap)
            // TODO: Proud skill extra level map
            // TODO: Server Buff List
            // TODO: Team resonance
            wearingFlycloakId = entity.data.wearingFlyCloakId.id
            bornTime = entity.data.bornTime.epochSeconds.toInt()
            // TODO: `pending_promote_reward_list`
            entity.data.costumeId?.let { costumeId = it }
            // curVehicleInfo
            excelInfo = protoExcelInfo
            // animHash
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Avatar.impl(): AbstractAvatar {
    contract { returns() implies (this@impl is AbstractAvatar) }
    check(this is AbstractAvatar) {
        "A Avatar instance is not instance of AbstractAvatar. Your instance: ${this::class.qualifiedOrSimple}"
    }
    return this
}
