package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AvatarDataItem(
    @SerialName("BodyType") val bodyType: String,
    @SerialName("ScriptDataPathHashSuffix") val scriptDataPathHashSuffix: Long,
    @SerialName("ScriptDataPathHashPre") val scriptDataPathHashPre: Int,
    @SerialName("IconName") val iconName: String,
    @SerialName("SideIconName") val sideIconName: String,
    @SerialName("QualityType") val qualityType: String,
    @SerialName("ChargeEfficiency") val chargeEfficiency: Double,
    @SerialName("CombatConfigHashSuffix") val combatConfigHashSuffix: Long,
    @SerialName("CombatConfigHashPre") val combatConfigHashPre: Int,
    @SerialName("InitialWeapon") val initialWeapon: Int,
    @SerialName("WeaponType") val weaponType: String,
    @SerialName("ManekinPathHashSuffix") val manekinPathHashSuffix: Long,
    @SerialName("ManekinPathHashPre") val manekinPathHashPre: Int,
    @SerialName("ImageName") val imageName: String,
    @SerialName("GachaCardNameHashSuffix") val gachaCardNameHashSuffix: Long,
    @SerialName("GachaImageNameHashSuffix") val gachaImageNameHashSuffix: Long,
    @SerialName("ControllerPathRemoteHashPre") val controllerPathRemoteHashPre: Int,
    @SerialName("CutsceneShow") val cutsceneShow: String,
    @SerialName("SkillDepotId") val skillDepotId: Int,
    @SerialName("StaminaRecoverSpeed") val staminaRecoverSpeed: Double,
    @SerialName("CandSkillDepotIds") val candSkillDepotIds: List<Int>,
    @SerialName("ManekinJsonConfigHashSuffix") val manekinJsonConfigHashSuffix: Long,
    @SerialName("ManekinJsonConfigHashPre") val manekinJsonConfigHashPre: Int,
    @SerialName("ManekinMotionConfig") val manekinMotionConfig: Int,
    @SerialName("DescTextMapHash") val descTextMapHash: Long,
    @SerialName("AvatarIdentityType") val avatarIdentityType: String,
    @SerialName("AvatarPromoteId") val avatarPromoteId: Int,
    @SerialName("AvatarPromoteRewardLevelList") val avatarPromoteRewardLevelList: List<Int>,
    @SerialName("AvatarPromoteRewardIdList") val avatarPromoteRewardIdList: List<Int>,
    @SerialName("FeatureTagGroupID") val featureTagGroupID: Int,
    @SerialName("InfoDescTextMapHash") val infoDescTextMapHash: Long,
    @SerialName("HpBase") val hpBase: Double,
    @SerialName("AttackBase") val attackBase: Double,
    @SerialName("DefenseBase") val defenseBase: Double,
    @SerialName("Critical") val critical: Double,
    @SerialName("CriticalHurt") val criticalHurt: Double,
    @SerialName("PropGrowCurves") val propGrowCurves: List<PropGrowCurve>,
    @SerialName("PrefabPathRagdollHashSuffix") val prefabPathRagdollHashSuffix: Long,
    @SerialName("PrefabPathRagdollHashPre") val prefabPathRagdollHashPre: Int,
    @SerialName("Id") val id: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("PrefabPathHashSuffix") val prefabPathHashSuffix: Long,
    @SerialName("PrefabPathHashPre") val prefabPathHashPre: Int,
    @SerialName("PrefabPathRemoteHashSuffix") val prefabPathRemoteHashSuffix: Long,
    @SerialName("PrefabPathRemoteHashPre") val prefabPathRemoteHashPre: Int,
    @SerialName("ControllerPathHashSuffix") val controllerPathHashSuffix: Long,
    @SerialName("ControllerPathHashPre") val controllerPathHashPre: Int,
    @SerialName("ControllerPathRemoteHashSuffix") val controllerPathRemoteHashSuffix: Long,
    @SerialName("LODPatternName") val lODPatternName: String,
    @SerialName("UseType") val useType: String,
    @SerialName("CoopPicNameHashSuffix") val coopPicNameHashSuffix: Long,
    @SerialName("IsRangeAttack") val isRangeAttack: Boolean
) {
    @Serializable
    data class PropGrowCurve(
        @SerialName("Type") val type: String,
        @SerialName("GrowCurve") val growCurve: String
    )
}
