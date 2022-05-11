package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MonsterData(
    @SerialName("MonsterName") val monsterName: String,
    @SerialName("Type") val type: String,
    @SerialName("ScriptDataPathHashSuffix") val scriptDataPathHashSuffix: Int,
    @SerialName("ScriptDataPathHashPre") val scriptDataPathHashPre: Int,
    @SerialName("ServerScript") val serverScript: String,
    @SerialName("CombatConfigHashSuffix") val combatConfigHashSuffix: Int,
    @SerialName("CombatConfigHashPre") val combatConfigHashPre: Int,
    @SerialName("Affix") val affix: List<Int>,
    @SerialName("Ai") val ai: String,
    @SerialName("IsInvisibleReset") val isInvisibleReset: Boolean,
    @SerialName("Equips") val equips: List<Int>,
    @SerialName("HpDrops") val hpDrops: List<HpDrop>,
    @SerialName("KillDropId") val killDropId: Int,
    @SerialName("ExcludeWeathers") val excludeWeathers: String,
    @SerialName("FeatureTagGroupID") val featureTagGroupID: Int,
    @SerialName("MpPropID") val mpPropID: Int,
    @SerialName("Skin") val skin: String,
    @SerialName("DescribeId") val describeId: Int,
    @SerialName("CombatBGMLevel") val combatBGMLevel: Int,
    @SerialName("EntityBudgetLevel") val entityBudgetLevel: Int,
    @SerialName("HpBase") val hpBase: Double,
    @SerialName("AttackBase") val attackBase: Double,
    @SerialName("DefenseBase") val defenseBase: Double,
    @SerialName("IceSubHurt") val iceSubHurt: Double,
    @SerialName("GrassSubHurt") val grassSubHurt: Double,
    @SerialName("WindSubHurt") val windSubHurt: Double,
    @SerialName("ElecSubHurt") val elecSubHurt: Double,
    @SerialName("PropGrowCurves") val propGrowCurves: List<PropGrowCurve>,
    @SerialName("PhysicalSubHurt") val physicalSubHurt: Double,
    @SerialName("PrefabPathRagdollHashSuffix") val prefabPathRagdollHashSuffix: Int,
    @SerialName("PrefabPathRagdollHashPre") val prefabPathRagdollHashPre: Int,
    @SerialName("Id") val id: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("PrefabPathHashSuffix") val prefabPathHashSuffix: Long,
    @SerialName("PrefabPathHashPre") val prefabPathHashPre: Int,
    @SerialName("PrefabPathRemoteHashSuffix") val prefabPathRemoteHashSuffix: Int,
    @SerialName("PrefabPathRemoteHashPre") val prefabPathRemoteHashPre: Int,
    @SerialName("ControllerPathHashSuffix") val controllerPathHashSuffix: Int,
    @SerialName("ControllerPathHashPre") val controllerPathHashPre: Int,
    @SerialName("ControllerPathRemoteHashSuffix") val controllerPathRemoteHashSuffix: Long,
    @SerialName("ControllerPathRemoteHashPre") val controllerPathRemoteHashPre: Int,
    @SerialName("CampID") val campID: Int,
    @SerialName("LODPatternName") val lODPatternName: String
) {
    @Serializable
    data class HpDrop(
        @SerialName("DropId") val dropId: Int,
        @SerialName("HpPercent") val hpPercent: Double
    )

    @Serializable
    data class PropGrowCurve(
        @SerialName("Type") val type: String,
        @SerialName("GrowCurve") val growCurve: String
    )
}
