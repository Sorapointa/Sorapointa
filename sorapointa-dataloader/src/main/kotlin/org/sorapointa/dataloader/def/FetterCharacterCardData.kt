package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val fetterCharacterCardDataLoader =
    DataLoader<List<FetterCharacterCardData>>("./ExcelBinOutput/FetterCharacterCardExcelConfigData.json")

val fetterCharacterCardData get() = fetterCharacterCardDataLoader.data

@Serializable
data class FetterCharacterCardData(
    @JsonNames("avatarId", "AvatarId")
    val avatarId: Int,
    @JsonNames("fetterLevel", "FetterLevel")
    val fetterLevel: Int,
    @JsonNames("rewardId", "RewardId")
    val rewardId: Int
)
