package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val monsterDataLoader =
    DataLoader<List<MonsterData>>("./ExcelBinOutput/MonsterExcelConfigData.json")

val monsterData get() = monsterDataLoader.data

@Serializable
data class MonsterData(
    @JsonNames("monsterName", "MonsterName")
    val monsterName: String,
    @JsonNames("type", "Type")
    val type: String,
    @JsonNames("scriptDataPathHashSuffix", "ScriptDataPathHashSuffix")
    val scriptDataPathHashSuffix: Int,
    @JsonNames("scriptDataPathHashPre", "ScriptDataPathHashPre")
    val scriptDataPathHashPre: Int,
    @JsonNames("serverScript", "ServerScript")
    val serverScript: String,
    @JsonNames("combatConfigHashSuffix", "CombatConfigHashSuffix")
    val combatConfigHashSuffix: Int,
    @JsonNames("combatConfigHashPre", "CombatConfigHashPre")
    val combatConfigHashPre: Int,
    @JsonNames("affix", "Affix")
    val affix: List<Int>,
    @JsonNames("ai", "Ai")
    val ai: String,
    @JsonNames("isInvisibleReset", "IsInvisibleReset")
    val isInvisibleReset: Boolean,
    @JsonNames("equips", "Equips")
    val equips: List<Int>,
    @JsonNames("hpDrops", "HpDrops")
    val hpDrops: List<HpDrop>,
    @JsonNames("killDropId", "KillDropId")
    val killDropId: Int,
    @JsonNames("excludeWeathers", "ExcludeWeathers")
    val excludeWeathers: String,
    @JsonNames("featureTagGroupID", "FeatureTagGroupID")
    val featureTagGroupID: Int,
    @JsonNames("mpPropID", "MpPropID")
    val mpPropID: Int,
    @JsonNames("skin", "Skin")
    val skin: String,
    @JsonNames("describeId", "DescribeId")
    val describeId: Int,
    @JsonNames("combatBGMLevel", "CombatBGMLevel")
    val combatBGMLevel: Int,
    @JsonNames("entityBudgetLevel", "EntityBudgetLevel")
    val entityBudgetLevel: Int,
    @JsonNames("hpBase", "HpBase")
    val hpBase: Double,
    @JsonNames("attackBase", "AttackBase")
    val attackBase: Double,
    @JsonNames("defenseBase", "DefenseBase")
    val defenseBase: Double,
    @JsonNames("iceSubHurt", "IceSubHurt")
    val iceSubHurt: Double,
    @JsonNames("grassSubHurt", "GrassSubHurt")
    val grassSubHurt: Double,
    @JsonNames("windSubHurt", "WindSubHurt")
    val windSubHurt: Double,
    @JsonNames("elecSubHurt", "ElecSubHurt")
    val elecSubHurt: Double,
    @JsonNames("propGrowCurves", "PropGrowCurves")
    val propGrowCurves: List<PropGrowCurve>,
    @JsonNames("physicalSubHurt", "PhysicalSubHurt")
    val physicalSubHurt: Double,
    @JsonNames("prefabPathRagdollHashSuffix", "PrefabPathRagdollHashSuffix")
    val prefabPathRagdollHashSuffix: Int,
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
    val prefabPathRemoteHashSuffix: Int,
    @JsonNames("prefabPathRemoteHashPre", "PrefabPathRemoteHashPre")
    val prefabPathRemoteHashPre: Int,
    @JsonNames("controllerPathHashSuffix", "ControllerPathHashSuffix")
    val controllerPathHashSuffix: Int,
    @JsonNames("controllerPathHashPre", "ControllerPathHashPre")
    val controllerPathHashPre: Int,
    @JsonNames("controllerPathRemoteHashSuffix", "ControllerPathRemoteHashSuffix")
    val controllerPathRemoteHashSuffix: Long,
    @JsonNames("controllerPathRemoteHashPre", "ControllerPathRemoteHashPre")
    val controllerPathRemoteHashPre: Int,
    @JsonNames("campID", "CampID")
    val campID: Int,
    @JsonNames("lODPatternName", "LODPatternName")
    val lODPatternName: String
) {
    @Serializable
    data class HpDrop(
        @JsonNames("dropId", "DropId")
        val dropId: Int,
        @JsonNames("hpPercent", "HpPercent")
        val hpPercent: Double
    )

    @Serializable
    data class PropGrowCurve(
        @JsonNames("type", "Type")
        val type: String,
        @JsonNames("growCurve", "GrowCurve")
        val growCurve: String
    )
}
