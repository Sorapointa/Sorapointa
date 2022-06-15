package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val rewardLoader =
    DataLoader<List<RewardData>>("./ExcelBinOutput/RewardExcelConfigData.json")

val rewardData get() = rewardLoader.data

@Serializable
data class RewardData(
    @JsonNames("rewardId", "RewardId")
    val rewardId: Int,
    @JsonNames("rewardItemList", "RewardItemList")
    val rewardItemList: List<RewardItem>,
    @JsonNames("scoin", "Scoin")
    val scoin: Int,
    @JsonNames("playerExp", "PlayerExp")
    val playerExp: Int,
    @JsonNames("hcoin", "Hcoin")
    val hcoin: Int
) {
    @Serializable
    data class RewardItem(
        @JsonNames("itemId", "ItemId")
        val itemId: Int,
        @JsonNames("itemCount", "ItemCount")
        val itemCount: Int
    )
}
