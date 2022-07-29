package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val avatarFetterLevelDataLoader =
    DataLoader<List<AvatarFetterLevelData>>("./ExcelBinOutput/AvatarFettersLevelExcelConfigData.json")

val avatarFetterLevelData get() = avatarFetterLevelDataLoader.data

@Serializable
data class AvatarFetterLevelData(
    @JsonNames("fetterLevel", "FetterLevel")
    val fetterLevel: Int,
    @JsonNames("needExp", "NeedExp")
    val needExp: Int
)
