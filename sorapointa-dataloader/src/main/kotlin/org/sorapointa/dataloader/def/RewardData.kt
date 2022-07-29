package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.ItemParamData

private val rewardLoader =
    DataLoader<List<RewardData>>("./ExcelBinOutput/RewardExcelConfigData.json")

val rewardData get() = rewardLoader.data

@Serializable
data class RewardData(
    @JsonNames("rewardId", "RewardId")
    val rewardId: Int,
    @JsonNames("rewardItemList", "RewardItemList")
    private val _rewardItemList: List<ItemParamData>,
    @JsonNames("scoin", "Scoin")
    val scoin: Int = 0,
    @JsonNames("playerExp", "PlayerExp")
    val playerExp: Int = 0,
    @JsonNames("hcoin", "Hcoin")
    val hcoin: Int = 0
) {

    val rewardItemList by lazy {
        _rewardItemList.filter { it.id != 0 }
    }
}
