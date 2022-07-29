package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class RewardItemData(
    @JsonNames("itemId", "ItemId") val itemId: Int,
    @JsonNames("itemCount", "ItemCount") val itemCount: Int,
)
