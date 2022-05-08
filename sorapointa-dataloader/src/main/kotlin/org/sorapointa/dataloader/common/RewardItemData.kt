package org.sorapointa.dataloader.common

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class RewardItemData(
    @SerialName("ItemId") val itemId: Int,
    @SerialName("ItemCount") val itemCount: Int,
)
