package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val worldLevelDataLoader =
    DataLoader<List<WorldLevelData>>("./ExcelBinOutput/WorldLevelExcelConfigData.json")

val worldLevelData get() = worldLevelDataLoader.data

@Serializable
data class WorldLevelData(
    @JsonNames("level", "Level")
    val level: Int,
    @JsonNames("monsterLevel", "MonsterLevel")
    val monsterLevel: Int
)
