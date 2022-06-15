package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val towerLevelLoader =
    DataLoader<List<TowerLevelData>>("./ExcelBinOutput/TowerLevelExcelConfigData.json")

val towerLevelData get() = towerLevelLoader.data

@Serializable
data class TowerLevelData(
    @JsonNames("iD", "ID")
    val iD: Int,
    @JsonNames("levelId", "LevelId")
    val levelId: Int,
    @JsonNames("levelIndex", "LevelIndex")
    val levelIndex: Int,
    @JsonNames("dungeonId", "DungeonId")
    val dungeonId: Int,
    @JsonNames("param", "Param")
    val `param`: List<Param>,
    @JsonNames("towerBuffConfigStrList", "TowerBuffConfigStrList")
    val towerBuffConfigStrList: List<String>,
    @JsonNames("firstPassRewardId", "FirstPassRewardId")
    val firstPassRewardId: Int,
    @JsonNames("monsterLevel", "MonsterLevel")
    val monsterLevel: Int,
    @JsonNames("firstMonsterList", "FirstMonsterList")
    val firstMonsterList: List<Int>,
    @JsonNames("secondMonsterList", "SecondMonsterList")
    val secondMonsterList: List<Int>
) {
    @Serializable
    data class Param(
        @JsonNames("towerCondType", "TowerCondType")
        val towerCondType: String,
        @JsonNames("argumentList", "ArgumentList")
        val argumentList: List<Int>
    )
}
