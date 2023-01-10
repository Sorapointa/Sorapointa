package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.*

private val avatarExcelDataLoader =
    DataLoader<List<AvatarExcelData>>("./ExcelBinOutput/AvatarExcelConfigData.json")

val avatarDataList get() = avatarExcelDataLoader.data

fun getAvatarExcelData(id: Int) =
    avatarDataList.firstOrNull { it.id == id }

@Serializable
data class AvatarExcelData(
    @JsonNames("scriptDataPathHash", "ScriptDataPathHash")
    val scriptDataPathHash: ULong,
    @JsonNames("iconName", "IconName")
    val iconName: String,
    @JsonNames("sideIconName", "SideIconName")
    val sideIconName: String,
    @JsonNames("chargeEfficiency", "ChargeEfficiency")
    val chargeEfficiency: Double,
    @JsonNames("combatConfigHash", "CombatConfigHash")
    val combatConfigHash: ULong,
    @JsonNames("initialWeapon", "InitialWeapon")
    val initialWeapon: Int,
    @JsonNames("weaponType", "WeaponType")
    private val _weaponType: JsonPrimitive,
    @JsonNames("manekinPathHash", "ManekinPathHash")
    val manekinPathHash: ULong,
    @JsonNames("imageName", "ImageName")
    val imageName: String,
    @JsonNames("gachaCardNameHash", "GachaCardNameHash")
    val gachaCardNameHash: ULong? = null,
    @JsonNames("gachaImageNameHash", "GachaImageNameHash")
    val gachaImageNameHash: ULong? = null,
    @JsonNames("skillDepotId", "SkillDepotId")
    val skillDepotId: Int,
    @JsonNames("staminaRecoverSpeed", "StaminaRecoverSpeed")
    val staminaRecoverSpeed: Double,
    @JsonNames("candSkillDepotIds", "CandSkillDepotIds")
    val candSkillDepotIds: List<Int>? = null,
    @JsonNames("manekinJsonConfigHash", "ManekinJsonConfigHash")
    val manekinJsonConfigHash: ULong,
    @JsonNames("manekinMotionConfig", "ManekinMotionConfig")
    val manekinMotionConfig: Int,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: ULong,
    @JsonNames("avatarPromoteId", "AvatarPromoteId")
    val avatarPromoteId: Int,
    @JsonNames("avatarPromoteRewardLevelList", "AvatarPromoteRewardLevelList")
    val avatarPromoteRewardLevelList: List<Int>? = null,
    @JsonNames("avatarPromoteRewardIdList", "AvatarPromoteRewardIdList")
    val avatarPromoteRewardIdList: List<Int>? = null,
    @JsonNames("featureTagGroupID", "FeatureTagGroupID", "featureTagGroupId")
    val featureTagGroupId: Int,
    @JsonNames("infoDescTextMapHash", "InfoDescTextMapHash")
    val infoDescTextMapHash: ULong,
    @JsonNames("hpBase", "HpBase")
    val hpBase: Double,
    @JsonNames("attackBase", "AttackBase")
    val attackBase: Double,
    @JsonNames("defenseBase", "DefenseBase")
    val defenseBase: Double,
    @JsonNames("critical", "Critical")
    val critical: Double,
    @JsonNames("criticalHurt", "CriticalHurt")
    val criticalHurt: Double,
    @JsonNames("propGrowCurves", "PropGrowCurves")
    private val _propGrowCurves: List<PropGrowCurve>,
    @JsonNames("prefabPathRagdollHash", "PrefabPathRagdollHash")
    val prefabPathRagdollHash: ULong,
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: ULong,
    @JsonNames("prefabPathHash", "PrefabPathHash")
    val prefabPathHash: ULong,
    @JsonNames("prefabPathRemoteHash", "PrefabPathRemoteHash")
    val prefabPathRemoteHash: ULong,
    @JsonNames("controllerPathHash", "ControllerPathHash")
    val controllerPathHash: ULong,
    @JsonNames("controllerPathRemoteHash", "ControllerPathRemoteHash")
    val controllerPathRemoteHash: ULong,
    @JsonNames("useType", "UseType")
    private val _useType: JsonPrimitive? = null,
    @JsonNames("isRangeAttack", "IsRangeAttack")
    val isRangeAttack: Boolean = false
) {
    val weaponType by lazy {
        acceptEnum(_weaponType, WeaponType.WEAPON_NONE)
    }

    val propGrowCurves by lazy {
        _propGrowCurves.filter { it.type != FightProp.FIGHT_PROP_NONE }
    }

    val useType by lazy {
        _useType?.let {
            acceptEnum(_useType, AvatarUseType.AVATAR_TEST)
        }
    }
}
