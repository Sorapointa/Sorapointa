@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val avatarLevelDataLoader =
    DataLoader<List<AvatarLevelData>>("./ExcelBinOutput/AvatarLevelExcelConfigData.json")

val avatarLevelData get() = avatarLevelDataLoader.data

@Serializable
data class AvatarLevelData(
    @JsonNames("level", "Level")
    val level: Int,
    @JsonNames("exp", "Exp")
    val exp: Int,
)
