package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.common.PropGrowCurve

@Serializable
data class MonsterData(
    @SerialName("Id") val id: Int,
    @SerialName("MonsterName") val monsterName: String,
    @SerialName("Type") val type: String,
    @SerialName("ServerScript") val serverScript: String,
    @SerialName("Affix") val affix: List<Int>,
    @SerialName("Ai") val ai: String,
    @SerialName("Equips") val equips: List<Int>, // Array
    @SerialName("HpDrops") val hpDrops: List<HpDrops>,
    @SerialName("KillDropId") val killDropId: Int,
    @SerialName("ExcludeWeathers") val excludeWeathers: String,
    @SerialName("FeatureTagGroupID") val featureTagGroupID: Int,
    @SerialName("MpPropID") val mpPropID: Int,
    @SerialName("Skin") val skin: String,
    @SerialName("DescribeId") val describeId: Int,
    @SerialName("CombatBGMLevel") val combatBGMLevel: Int,
    @SerialName("EntityBudgetLevel") val entityBudgetLevel: Int,
    @SerialName("HpBase") val hpBase: Float,
    @SerialName("AttackBase") val attackBase: Float,
    @SerialName("DefenseBase") val defenseBase: Float,
    @SerialName("FireSubHurt") val fireSubHurt: Float,
    @SerialName("ElecSubHurt") val elecSubHurt: Float,
    @SerialName("GrassSubHurt") val grassSubHurt: Float,
    @SerialName("WaterSubHurt") val waterSubHurt: Float,
    @SerialName("WindSubHurt") val windSubHurt: Float,
    @SerialName("RockSubHurt") val rockSubHurt: Float,
    @SerialName("IceSubHurt") val iceSubHurt: Float,
    @SerialName("PhysicalSubHurt") val physicalSubHurt: Float,
    @SerialName("PropGrowCurves") val propGrowCurves: List<PropGrowCurve>,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("CampID") val campID: Int,
    @SerialName("WeaponId") val weaponId: Int,
    @SerialName("DescribeData") val describeData: MonsterDescribeData,
) {
    @Serializable
    data class HpDrops(
        @SerialName("DropId") val dropId: Int,
        @SerialName("HpPercent") val hpPercent: Int,
    )
}
