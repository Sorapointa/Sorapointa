package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class RewardDataItem(
    @SerialName("RewardId") val rewardId: Int,
    @SerialName("RewardItemList") val rewardItemList: List<RewardItem>,
    @SerialName("Scoin") val scoin: Int,
    @SerialName("PlayerExp") val playerExp: Int,
    @SerialName("Hcoin") val hcoin: Int
) {
    @Serializable
    data class RewardItem(
        @SerialName("ItemId") val itemId: Int,
        @SerialName("ItemCount") val itemCount: Int
    )
}
