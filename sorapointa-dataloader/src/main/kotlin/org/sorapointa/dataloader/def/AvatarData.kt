package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.common.PropGrowCurve

@Serializable
data class AvatarData(
    @SerialName("IconName") val iconName: String,
    @SerialName("BodyType") val bodyType: String,
    @SerialName("QualityType") val qualityType: String,
    @SerialName("ChargeEfficiency") val chargeEfficiency: Int,
    @SerialName("InitialWeapon") val initialWeapon: Int,
    @SerialName("WeaponType") val weaponType: String,
    @SerialName("ImageName") val imageName: String,
    @SerialName("AvatarPromoteId") val avatarPromoteId: Int,
    @SerialName("CutsceneShow") val cutsceneShow: String,
    @SerialName("SkillDepotId") val skillDepotId: Int,
    @SerialName("StaminaRecoverSpeed") val staminaRecoverSpeed: Int,
    @SerialName("CandSkillDepotIds") val candSkillDepotIds: List<Int>, // String in GC but Int in JSON?
    @SerialName("DescTextMapHash") val descTextMapHash: Long,
    @SerialName("AvatarIdentityType") val avatarIdentityType: String,
    @SerialName("AvatarPromoteRewardLevelList") val avatarPromoteRewardLevelList: List<Int>,
    @SerialName("AvatarPromoteRewardIdList") val avatarPromoteRewardIdList: List<Int>,
    @SerialName("FeatureTagGroupID") val featureTagGroupID: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("GachaImageNameHashSuffix") val gachaImageNameHashSuffix: Long,
    @SerialName("InfoDescTextMapHash") val infoDescTextMapHash: Long,
    @SerialName("HpBase") val hpBase: Float,
    @SerialName("AttackBase") val attackBase: Float,
    @SerialName("DefenseBase") val defenseBase: Float,
    @SerialName("Critical") val critical: Float,
    @SerialName("CriticalHurt") val criticalHurt: Float,
    @SerialName("PropGrowCurves") val propGrowCurves: List<PropGrowCurve>,
    @SerialName("Id") val id: Int,
)
