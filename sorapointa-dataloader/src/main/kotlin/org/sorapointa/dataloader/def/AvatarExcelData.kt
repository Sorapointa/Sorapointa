@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.utils.encoding.getHashByPrefixSuffix

private val avatarExcelDataLoader =
    DataLoader<List<AvatarExcelData>>("./ExcelBinOutput/AvatarExcelConfigData.json")

val avatarDataList get() = avatarExcelDataLoader.data

fun getAvatarExcelData(id: Int) =
    avatarDataList.firstOrNull { it.id == id }

@Serializable
data class AvatarExcelData(
    @JsonNames("bodyType", "BodyType")
    val bodyType: String,
    @JsonNames("scriptDataPathHashSuffix", "ScriptDataPathHashSuffix")
    val scriptDataPathHashSuffix: Long,
    @JsonNames("scriptDataPathHashPre", "ScriptDataPathHashPre")
    val scriptDataPathHashPre: Int,
    @JsonNames("iconName", "IconName")
    val iconName: String,
    @JsonNames("sideIconName", "SideIconName")
    val sideIconName: String,
    @JsonNames("qualityType", "QualityType")
    val qualityType: String,
    @JsonNames("chargeEfficiency", "ChargeEfficiency")
    val chargeEfficiency: Double,
    @JsonNames("combatConfigHashSuffix", "CombatConfigHashSuffix")
    val combatConfigHashSuffix: Long,
    @JsonNames("combatConfigHashPre", "CombatConfigHashPre")
    val combatConfigHashPre: Int,
    @JsonNames("initialWeapon", "InitialWeapon")
    val initialWeapon: Int,
    @JsonNames("weaponType", "WeaponType")
    val weaponType: String,
    @JsonNames("manekinPathHashSuffix", "ManekinPathHashSuffix")
    val manekinPathHashSuffix: Long,
    @JsonNames("manekinPathHashPre", "ManekinPathHashPre")
    val manekinPathHashPre: Int,
    @JsonNames("imageName", "ImageName")
    val imageName: String,
    @JsonNames("gachaCardNameHashSuffix", "GachaCardNameHashSuffix")
    val gachaCardNameHashSuffix: Long,
    @JsonNames("gachaImageNameHashSuffix", "GachaImageNameHashSuffix")
    val gachaImageNameHashSuffix: Long,
    @JsonNames("controllerPathRemoteHashPre", "ControllerPathRemoteHashPre")
    val controllerPathRemoteHashPre: Int,
    @JsonNames("cutsceneShow", "CutsceneShow")
    val cutsceneShow: String,
    @JsonNames("skillDepotId", "SkillDepotId")
    val skillDepotId: Int,
    @JsonNames("staminaRecoverSpeed", "StaminaRecoverSpeed")
    val staminaRecoverSpeed: Double,
    @JsonNames("candSkillDepotIds", "CandSkillDepotIds")
    val candSkillDepotIds: List<Int>,
    @JsonNames("manekinJsonConfigHashSuffix", "ManekinJsonConfigHashSuffix")
    val manekinJsonConfigHashSuffix: Long,
    @JsonNames("manekinJsonConfigHashPre", "ManekinJsonConfigHashPre")
    val manekinJsonConfigHashPre: Int,
    @JsonNames("manekinMotionConfig", "ManekinMotionConfig")
    val manekinMotionConfig: Int,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("avatarIdentityType", "AvatarIdentityType")
    val avatarIdentityType: String,
    @JsonNames("avatarPromoteId", "AvatarPromoteId")
    val avatarPromoteId: Int,
    @JsonNames("avatarPromoteRewardLevelList", "AvatarPromoteRewardLevelList")
    val avatarPromoteRewardLevelList: List<Int>,
    @JsonNames("avatarPromoteRewardIdList", "AvatarPromoteRewardIdList")
    val avatarPromoteRewardIdList: List<Int>,
    @JsonNames("featureTagGroupID", "FeatureTagGroupID")
    val featureTagGroupID: Int,
    @JsonNames("infoDescTextMapHash", "InfoDescTextMapHash")
    val infoDescTextMapHash: Long,
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
    val propGrowCurves: List<PropGrowCurve>,
    @JsonNames("prefabPathRagdollHashSuffix", "PrefabPathRagdollHashSuffix")
    val prefabPathRagdollHashSuffix: Long,
    @JsonNames("prefabPathRagdollHashPre", "PrefabPathRagdollHashPre")
    val prefabPathRagdollHashPre: Int,
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("prefabPathHashSuffix", "PrefabPathHashSuffix")
    val prefabPathHashSuffix: Long,
    @JsonNames("prefabPathHashPre", "PrefabPathHashPre")
    val prefabPathHashPre: Int,
    @JsonNames("prefabPathRemoteHashSuffix", "PrefabPathRemoteHashSuffix")
    val prefabPathRemoteHashSuffix: Long,
    @JsonNames("prefabPathRemoteHashPre", "PrefabPathRemoteHashPre")
    val prefabPathRemoteHashPre: Int,
    @JsonNames("controllerPathHashSuffix", "ControllerPathHashSuffix")
    val controllerPathHashSuffix: Long,
    @JsonNames("controllerPathHashPre", "ControllerPathHashPre")
    val controllerPathHashPre: Int,
    @JsonNames("controllerPathRemoteHashSuffix", "ControllerPathRemoteHashSuffix")
    val controllerPathRemoteHashSuffix: Long,
    @JsonNames("lODPatternName", "LODPatternName")
    val lODPatternName: String,
    @JsonNames("useType", "UseType")
    val useType: String,
    @JsonNames("coopPicNameHashSuffix", "CoopPicNameHashSuffix")
    val coopPicNameHashSuffix: Long,
    @JsonNames("isRangeAttack", "IsRangeAttack")
    val isRangeAttack: Boolean
) {

    val prefabPathHash by lazy {
        getHashByPrefixSuffix(prefabPathHashPre, prefabPathHashSuffix)
    }

    val prefabPathRemoteHash by lazy {
        getHashByPrefixSuffix(prefabPathRemoteHashPre, prefabPathRemoteHashSuffix)
    }

    val controllerPathHash by lazy {
        getHashByPrefixSuffix(controllerPathHashPre, controllerPathHashSuffix)
    }

    val controllerPathRemoteHash by lazy {
        getHashByPrefixSuffix(controllerPathRemoteHashPre, controllerPathRemoteHashSuffix)
    }

    val combatConfigHash by lazy {
        getHashByPrefixSuffix(combatConfigHashPre, combatConfigHashSuffix)
    }

    @Serializable
    data class PropGrowCurve(
        @JsonNames("type", "Type")
        val type: String,
        @JsonNames("growCurve", "GrowCurve")
        val growCurve: String
    )
}
