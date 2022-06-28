package org.sorapointa.game.data

import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.select
import org.sorapointa.dataloader.common.*
import org.sorapointa.dataloader.common.FightProp.*
import org.sorapointa.dataloader.common.PlayerProp.*
import org.sorapointa.game.Avatar
import org.sorapointa.game.AvatarImpl
import org.sorapointa.game.Player
import org.sorapointa.game.impl
import org.sorapointa.server.network.AvatarFightPropUpdateNotifyPacket
import org.sorapointa.server.network.AvatarPropNotifyPacket
import org.sorapointa.utils.SQLPropDelegate
import org.sorapointa.utils.now

internal object AvatarDataTable : IdTable<Long>("avatar") {

    override val id: Column<EntityID<Long>> = long("avatar_guid").entityId()
    val ownerId: Column<EntityID<Int>> =
        OpenStateSetTable.reference("owner_id", PlayerDataTable, onDelete = ReferenceOption.CASCADE)
    val avatarId: Column<Int> = integer("avatar_id")
    val bornTime: Column<Instant> = timestamp("born_time")
    val costumeId: Column<Int?> = integer("costume_id").nullable()

    val lifeState: Column<LifeState> = enumeration<LifeState>("life_state")
        .default(LifeState.LIFE_ALIVE)
    val constellationLevel: Column<Int> = integer("constellation_level")
        .default(0)
    val wearingFlyCloakId: Column<FlyCloakId> = enumeration<FlyCloakId>("wearing_fly_cloak_id")
        .default(FlyCloakId.GLIDER)

    val level: Column<Int> = integer("level")
        .default(1)
    val exp: Column<Int> = integer("exp")
        .default(0)
    val promoteLevel: Column<Int> = integer("promote_level")
        .default(0)
    val satiationVal: Column<Int> = integer("satiation_val")
        .default(0)
    val satiationPenaltyTime: Column<Int> = integer("satiation_penalty_time")
        .default(0)

    val skillDepotId: Column<Int> = integer("skill_depot_id")
        .default(0)

    val normalAttackLevel: Column<Int> = integer("normal_attack_level")
        .default(1)
    val elementSkillLevel: Column<Int> = integer("element_skill_level")
        .default(1)
    val energySkillLevel: Column<Int> = integer("energy_skill_level")
        .default(1)

    val equipFlower: Column<Long?> =
        optReference("equip_flower", InventoryTable.key, onDelete = ReferenceOption.SET_NULL)
    val equipPlume: Column<Long?> =
        optReference("equip_plume", InventoryTable.key, onDelete = ReferenceOption.SET_NULL)
    val equipSands: Column<Long?> =
        optReference("equip_sands", InventoryTable.key, onDelete = ReferenceOption.SET_NULL)
    val equipGoblet: Column<Long?> =
        optReference("equip_goblet", InventoryTable.key, onDelete = ReferenceOption.SET_NULL)
    val equipCirclet: Column<Long?> =
        optReference("equip_circlet", InventoryTable.key, onDelete = ReferenceOption.SET_NULL)
    val equipWeapon: Column<Long?> =
        optReference("equip_weapon", InventoryTable.key, onDelete = ReferenceOption.SET_NULL)

    val baseHp: Column<Float> = float("base_hp").default(1f)
    val hp: Column<Float> = float("hp").default(0f)
    val hpPercent: Column<Float> = float("hp_percent").default(0f)
    val baseAttack: Column<Float> = float("base_attack").default(1f)
    val attack: Column<Float> = float("attack").default(0f)
    val attackPercent: Column<Float> = float("attack_percent").default(0f)
    val baseDefense: Column<Float> = float("base_defense").default(1f)
    val defense: Column<Float> = float("defense").default(0f)
    val defensePercent: Column<Float> = float("defense_percent").default(0f)
    val baseSpeed: Column<Float> = float("base_speed").default(0f)
    val speedPercent: Column<Float> = float("speed_percent").default(0f)
    val critical: Column<Float> = float("critical").default(0f)
    val antiCritical: Column<Float> = float("anti_critical").default(0f)
    val criticalHurt: Column<Float> = float("critical_hurt").default(0f)
    val chargeEfficiency: Column<Float> = float("charge_efficiency").default(1f)
    val addHurt: Column<Float> = float("add_hurt").default(0f)
    val subHurt: Column<Float> = float("sub_hurt").default(0f)
    val healAdd: Column<Float> = float("heal_add").default(0f)
    val healedAdd: Column<Float> = float("healed_add").default(0f)
    val elementMastery: Column<Float> = float("element_mastery").default(0f)
    val physicalSubHurt: Column<Float> = float("physical_sub_hurt").default(0f)
    val physicalAddHurt: Column<Float> = float("physical_add_hurt").default(0f)
    val defenceIgnoreRatio: Column<Float> = float("defence_ignore_ratio").default(0f)
    val defenceIgnoreDelta: Column<Float> = float("defence_ignore_delta").default(0f)
    val fireAddHurt: Column<Float> = float("fire_add_hurt").default(0f)
    val electricAddHurt: Column<Float> = float("electric_add_hurt").default(0f)
    val waterAddHurt: Column<Float> = float("water_add_hurt").default(0f)
    val grassAddHurt: Column<Float> = float("grass_add_hurt").default(0f)
    val windAddHurt: Column<Float> = float("wind_add_hurt").default(0f)
    val rockAddHurt: Column<Float> = float("rock_add_hurt").default(0f)
    val iceAddHurt: Column<Float> = float("ice_add_hurt").default(0f)
    val hitHeadAddHurt: Column<Float> = float("hit_head_add_hurt").default(0f)
    val fireSubHurt: Column<Float> = float("fire_sub_hurt").default(0f)
    val electricSubHurt: Column<Float> = float("electric_sub_hurt").default(0f)
    val waterSubHurt: Column<Float> = float("water_sub_hurt").default(0f)
    val grassSubHurt: Column<Float> = float("grass_sub_hurt").default(0f)
    val windSubHurt: Column<Float> = float("wind_sub_hurt").default(0f)
    val rockSubHurt: Column<Float> = float("rock_sub_hurt").default(0f)
    val iceSubHurt: Column<Float> = float("ice_sub_hurt").default(0f)

    // effectHit, effectResist, freezeResist, dizzyResist, freezeShorten, dizzyShorten
    val skillCDMinusRatio: Column<Float> = float("skill_cd_minus_ratio").default(0f)
    val shieldCostMinusRatio: Column<Float> = float("shield_cost_minus_ratio").default(0f)

    val currentEnergy: Column<Int> = integer("current_energy").default(0)
    val currentHP: Column<Float> = float("current_hp").default(1f)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

// TODO: AvatarSkillMap for storing CD time

interface AvatarData {

    val guid: Long

    val ownerId: EntityID<Int>
    val avatarId: Int
    var bornTime: Instant
    var costumeId: Int?

    var lifeState: LifeState
    var constellationLevel: Int
    var wearingFlyCloakId: FlyCloakId

    var level: Int
    var exp: Int
    var promoteLevel: Int
    var satiationVal: Int
    var satiationPenaltyTime: Int

    var skillDepotId: Int
    var normalAttackLevel: Int
    var elementSkillLevel: Int
    var energySkillLevel: Int

    var equipFlower: Long?
    var equipPlume: Long?
    var equipSands: Long?
    var equipGoblet: Long?
    var equipCirclet: Long?
    var equipWeapon: Long?

    var baseHp: Float
    var hp: Float
    var hpPercent: Float
    var baseAttack: Float
    var attack: Float
    var attackPercent: Float
    var baseDefense: Float
    var defense: Float
    var defensePercent: Float
    var baseSpeed: Float
    var speedPercent: Float
    var critical: Float
    var antiCritical: Float
    var criticalHurt: Float
    var chargeEfficiency: Float
    var addHurt: Float
    var subHurt: Float
    var healAdd: Float
    var healedAdd: Float
    var elementMastery: Float
    var physicalSubHurt: Float
    var physicalAddHurt: Float
    var defenceIgnoreRatio: Float
    var defenceIgnoreDelta: Float
    var fireAddHurt: Float
    var electricAddHurt: Float
    var waterAddHurt: Float
    var grassAddHurt: Float
    var windAddHurt: Float
    var rockAddHurt: Float
    var iceAddHurt: Float
    var hitHeadAddHurt: Float
    var fireSubHurt: Float
    var electricSubHurt: Float
    var waterSubHurt: Float
    var grassSubHurt: Float
    var windSubHurt: Float
    var rockSubHurt: Float
    var iceSubHurt: Float

    var skillCDMinusRatio: Float
    var shieldCostMinusRatio: Float

    var currentEnergy: Int
    var currentHP: Float
}

class AvatarDataImpl(id: EntityID<Long>) : Entity<Long>(id), AvatarData {

    private var avatar: Avatar? = null

    companion object : EntityClass<Long, AvatarDataImpl>(AvatarDataTable) {

        fun findAll(owner: Player): List<Avatar> =
            AvatarDataTable
                .slice(AvatarDataTable.id)
                .select { AvatarDataTable.ownerId eq owner.uid }
                .asSequence()
                .mapNotNull { findById(it[AvatarDataTable.id]) }
                .map { createAvatarInstance(owner, it) }
                .toList()

        fun find(guid: Long, owner: Player): Avatar? =
            findById(guid)?.let { data -> createAvatarInstance(owner, data) }

        fun create(guid: Long, owner: Player, avatarId: Int): Avatar =
            createAvatarInstance(
                owner,
                findById(
                    AvatarDataTable.insertAndGetId {
                        it[id] = guid
                        it[this.ownerId] = owner.uid
                        it[this.avatarId] = avatarId
                        it[bornTime] = now()
                    }
                ) ?: error("Could not create avatar data into database")
            ).also {
                it.initNewAvatar()
            }

        private fun createAvatarInstance(
            owner: Player,
            data: AvatarDataImpl,
            avatarType: AvatarType = AvatarType.FORMAL
        ) = AvatarImpl(owner, data, avatarType).also {
            data.avatar = it
        }
    }

    override val guid = id.value

    override val ownerId by AvatarDataTable.ownerId
    override val avatarId by AvatarDataTable.avatarId
    override var bornTime by AvatarDataTable.bornTime
    override var costumeId by AvatarDataTable.costumeId

    override var lifeState by AvatarDataTable.lifeState
    override var constellationLevel by AvatarDataTable.constellationLevel
    override var wearingFlyCloakId by AvatarDataTable.wearingFlyCloakId

    override var level by AvatarDataTable.level playerProp PROP_LEVEL
    override var exp by AvatarDataTable.exp playerProp PROP_EXP
    override var promoteLevel by AvatarDataTable.promoteLevel playerProp PROP_BREAK_LEVEL
    override var satiationVal by AvatarDataTable.satiationVal playerProp PROP_SATIATION_VAL
    override var satiationPenaltyTime by AvatarDataTable.satiationPenaltyTime playerProp PROP_SATIATION_PENALTY_TIME

    override var skillDepotId by AvatarDataTable.skillDepotId

    override var normalAttackLevel by AvatarDataTable.normalAttackLevel
    override var elementSkillLevel by AvatarDataTable.elementSkillLevel
    override var energySkillLevel by AvatarDataTable.energySkillLevel

    override var equipFlower by AvatarDataTable.equipFlower
    override var equipPlume by AvatarDataTable.equipPlume
    override var equipSands by AvatarDataTable.equipSands
    override var equipGoblet by AvatarDataTable.equipGoblet
    override var equipCirclet by AvatarDataTable.equipCirclet
    override var equipWeapon by AvatarDataTable.equipWeapon

    override var baseHp by AvatarDataTable.baseHp fightProp FIGHT_PROP_BASE_HP
    override var hp by AvatarDataTable.hp fightProp FIGHT_PROP_HP
    override var hpPercent by AvatarDataTable.hpPercent fightProp FIGHT_PROP_HP_PERCENT
    override var baseAttack by AvatarDataTable.baseAttack fightProp FIGHT_PROP_BASE_ATTACK
    override var attack by AvatarDataTable.attack fightProp FIGHT_PROP_ATTACK
    override var attackPercent by AvatarDataTable.attackPercent fightProp FIGHT_PROP_ATTACK_PERCENT
    override var baseDefense by AvatarDataTable.baseDefense fightProp FIGHT_PROP_BASE_DEFENSE
    override var defense by AvatarDataTable.defense fightProp FIGHT_PROP_DEFENSE
    override var defensePercent by AvatarDataTable.defensePercent fightProp FIGHT_PROP_DEFENSE_PERCENT
    override var baseSpeed by AvatarDataTable.baseSpeed fightProp FIGHT_PROP_BASE_SPEED
    override var speedPercent by AvatarDataTable.speedPercent fightProp FIGHT_PROP_SPEED_PERCENT
    override var critical by AvatarDataTable.critical fightProp FIGHT_PROP_CRITICAL
    override var antiCritical by AvatarDataTable.antiCritical fightProp FIGHT_PROP_ANTI_CRITICAL
    override var criticalHurt by AvatarDataTable.criticalHurt fightProp FIGHT_PROP_CRITICAL_HURT
    override var chargeEfficiency by AvatarDataTable.chargeEfficiency fightProp FIGHT_PROP_CHARGE_EFFICIENCY
    override var addHurt by AvatarDataTable.addHurt fightProp FIGHT_PROP_ADD_HURT
    override var subHurt by AvatarDataTable.subHurt fightProp FIGHT_PROP_SUB_HURT
    override var healAdd by AvatarDataTable.healAdd fightProp FIGHT_PROP_HEAL_ADD
    override var healedAdd by AvatarDataTable.healedAdd fightProp FIGHT_PROP_HEALED_ADD
    override var elementMastery by AvatarDataTable.elementMastery fightProp FIGHT_PROP_ELEMENT_MASTERY
    override var physicalSubHurt by AvatarDataTable.physicalSubHurt fightProp FIGHT_PROP_PHYSICAL_SUB_HURT
    override var physicalAddHurt by AvatarDataTable.physicalAddHurt fightProp FIGHT_PROP_PHYSICAL_ADD_HURT
    override var defenceIgnoreRatio by AvatarDataTable.defenceIgnoreRatio fightProp FIGHT_PROP_DEFENCE_IGNORE_RATIO
    override var defenceIgnoreDelta by AvatarDataTable.defenceIgnoreDelta fightProp FIGHT_PROP_DEFENCE_IGNORE_DELTA
    override var fireAddHurt by AvatarDataTable.fireAddHurt fightProp FIGHT_PROP_FIRE_ADD_HURT
    override var electricAddHurt by AvatarDataTable.electricAddHurt fightProp FIGHT_PROP_ELEC_ADD_HURT
    override var waterAddHurt by AvatarDataTable.waterAddHurt fightProp FIGHT_PROP_WATER_ADD_HURT
    override var grassAddHurt by AvatarDataTable.grassAddHurt fightProp FIGHT_PROP_GRASS_ADD_HURT
    override var windAddHurt by AvatarDataTable.windAddHurt fightProp FIGHT_PROP_WIND_ADD_HURT
    override var rockAddHurt by AvatarDataTable.rockAddHurt fightProp FIGHT_PROP_ROCK_ADD_HURT
    override var iceAddHurt by AvatarDataTable.iceAddHurt fightProp FIGHT_PROP_ICE_ADD_HURT
    override var hitHeadAddHurt by AvatarDataTable.hitHeadAddHurt fightProp FIGHT_PROP_HIT_HEAD_ADD_HURT
    override var fireSubHurt by AvatarDataTable.fireSubHurt fightProp FIGHT_PROP_FIRE_SUB_HURT
    override var electricSubHurt by AvatarDataTable.electricSubHurt fightProp FIGHT_PROP_ELEC_SUB_HURT
    override var waterSubHurt by AvatarDataTable.waterSubHurt fightProp FIGHT_PROP_WATER_SUB_HURT
    override var grassSubHurt by AvatarDataTable.grassSubHurt fightProp FIGHT_PROP_GRASS_SUB_HURT
    override var windSubHurt by AvatarDataTable.windSubHurt fightProp FIGHT_PROP_WIND_SUB_HURT
    override var rockSubHurt by AvatarDataTable.rockSubHurt fightProp FIGHT_PROP_ROCK_SUB_HURT
    override var iceSubHurt by AvatarDataTable.iceSubHurt fightProp FIGHT_PROP_ICE_SUB_HURT
    override var skillCDMinusRatio by AvatarDataTable.skillCDMinusRatio fightProp FIGHT_PROP_SKILL_CD_MINUS_RATIO
    override var shieldCostMinusRatio
        by AvatarDataTable.shieldCostMinusRatio fightProp FIGHT_PROP_SHIELD_COST_MINUS_RATIO

        override var currentEnergy by AvatarDataTable.currentEnergy fightProp avatar?.elementType?.curEnergyProp
        override var currentHP by AvatarDataTable.currentHP fightProp FIGHT_PROP_CUR_HP

        private infix fun <T : Number> Column<T>.playerProp(playerProp: PlayerProp?) =
            SQLPropDelegate(this@AvatarDataImpl, this, playerProp) { prop, value ->
                val avatar = avatar ?: return@SQLPropDelegate

                avatar.ownerPlayer.impl().sendPacket(
                    AvatarPropNotifyPacket(
                        avatar = avatar,
                        propMap = mapOf(prop.value to value.toLong())
                    )
                )
            }

        private infix fun <T : Number> Column<T>.fightProp(fightProp: FightProp?) =
            SQLPropDelegate(this@AvatarDataImpl, this, fightProp) { prop, value ->
                val avatar = avatar ?: return@SQLPropDelegate
                avatar.ownerPlayer.impl().sendPacket(
                    AvatarFightPropUpdateNotifyPacket(
                        avatar = avatar,
                        propMap = mapOf(prop.value to value.toFloat())
                    )
                )
            }
    }
    