package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TowerLevelData(
    @SerialName("ID") val iD: Int,
    @SerialName("LevelId") val levelId: Int,
    @SerialName("LevelIndex") val levelIndex: Int,
    @SerialName("DungeonId") val dungeonId: Int,
    @SerialName("Param") val `param`: List<Param>,
    @SerialName("TowerBuffConfigStrList") val towerBuffConfigStrList: List<String>,
    @SerialName("FirstPassRewardId") val firstPassRewardId: Int,
    @SerialName("MonsterLevel") val monsterLevel: Int,
    @SerialName("FirstMonsterList") val firstMonsterList: List<Int>,
    @SerialName("SecondMonsterList") val secondMonsterList: List<Int>
) {
    @Serializable
    data class Param(
        @SerialName("TowerCondType") val towerCondType: String,
        @SerialName("ArgumentList") val argumentList: List<Int>
    )
}
