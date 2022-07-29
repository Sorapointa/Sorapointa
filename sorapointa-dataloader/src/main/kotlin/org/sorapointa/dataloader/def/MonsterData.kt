package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.FightProp
import org.sorapointa.dataloader.common.PropGrowCurve

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
    val scriptDataPathHashSuffix: Long,
    @JsonNames("scriptDataPathHashPre", "ScriptDataPathHashPre")
    val scriptDataPathHashPre: Int,
    @JsonNames("serverScript", "ServerScript")
    val serverScript: String,
    @JsonNames("combatConfigHashSuffix", "CombatConfigHashSuffix")
    val combatConfigHashSuffix: Long,
    @JsonNames("combatConfigHashPre", "CombatConfigHashPre")
    val combatConfigHashPre: Int,
    @JsonNames("affix", "Affix")
    val affix: List<Int>,
    @JsonNames("ai", "Ai")
    val ai: String,
    @JsonNames("isInvisibleReset", "IsInvisibleReset")
    val isInvisibleReset: Boolean = false,
    @JsonNames("equips", "Equips")
    val equips: List<Int>,
    @JsonNames("hpDrops", "HpDrops")
    private val _hpDrops: List<HpDrop>,
    @JsonNames("killDropId", "KillDropId")
    val killDropId: Int? = null,
    @JsonNames("excludeWeathers", "ExcludeWeathers")
    val excludeWeathers: String,
    @JsonNames("featureTagGroupID", "FeatureTagGroupID")
    val featureTagGroupID: Int,
    @JsonNames("mpPropID", "MpPropID")
    val mpPropID: Int,
    @JsonNames("skin", "Skin")
    val skin: String,
    @JsonNames("describeId", "DescribeId")
    val describeId: Int? = null,
    @JsonNames("entityBudgetLevel", "EntityBudgetLevel")
    val entityBudgetLevel: Int? = null,
    @JsonNames("hpBase", "HpBase")
    val hpBase: Double,
    @JsonNames("attackBase", "AttackBase")
    val attackBase: Double = 0.0,
    @JsonNames("defenseBase", "DefenseBase")
    val defenseBase: Double = 0.0,
    @JsonNames("iceSubHurt", "IceSubHurt")
    val iceSubHurt: Double = 0.0,
    @JsonNames("grassSubHurt", "GrassSubHurt")
    val grassSubHurt: Double = 0.0,
    @JsonNames("windSubHurt", "WindSubHurt")
    val windSubHurt: Double = 0.0,
    @JsonNames("elecSubHurt", "ElecSubHurt")
    val elecSubHurt: Double = 0.0,
    @JsonNames("propGrowCurves", "PropGrowCurves")
    private val _propGrowCurves: List<PropGrowCurve>,
    @JsonNames("physicalSubHurt", "PhysicalSubHurt")
    val physicalSubHurt: Double = 0.0,
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
    @JsonNames("controllerPathRemoteHashPre", "ControllerPathRemoteHashPre")
    val controllerPathRemoteHashPre: Int,
    @JsonNames("campID", "CampID")
    val campID: Int,
    @JsonNames("lODPatternName", "LODPatternName")
    val lODPatternName: String
) {

    val hpDrops by lazy {
        _hpDrops.filter { it.dropId != 0 }
    }

    val propGrowCurves by lazy {
        _propGrowCurves.filter { it.type != FightProp.FIGHT_PROP_NONE }
    }

    @Serializable
    data class HpDrop(
        @JsonNames("dropId", "DropId")
        val dropId: Int = 0,
        @JsonNames("hpPercent", "HpPercent")
        val hpPercent: Double = 0.0
    )
}
