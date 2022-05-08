package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class TowerLevelData(
    @SerialName("ID") val id: Int,
    @SerialName("LevelId") val levelId: Int,
    @SerialName("LevelIndex") val levelIndex: Int,
    @SerialName("DungeonId") val dungeonId: Int,
)
