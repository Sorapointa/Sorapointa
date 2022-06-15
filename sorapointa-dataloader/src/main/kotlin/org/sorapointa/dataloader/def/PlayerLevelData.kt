package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val playerLevelLoader =
    DataLoader<List<PlayerLevelData>>("./ExcelBinOutput/PlayerLevelExcelConfigData.json")

val playerLevelData get() = playerLevelLoader.data

@Serializable
data class PlayerLevelData(
    @JsonNames("level", "Level")
    val level: Int,
    @JsonNames("exp", "Exp")
    val exp: Int,
    @JsonNames("unlockDescTextMapHash", "UnlockDescTextMapHash")
    val unlockDescTextMapHash: Long
)
