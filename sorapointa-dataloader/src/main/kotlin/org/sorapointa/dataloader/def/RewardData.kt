package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import org.sorapointa.dataloader.common.RewardItemData

@kotlinx.serialization.Serializable
data class RewardData(
    @SerialName("RewardId") val rewardId: Int,
    @SerialName("RewardItemList") val rewardItemList: List<RewardItemData>,
)
