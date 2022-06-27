package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ItemParamData(
    @JsonNames("id", "Id", "ItemId", "itemId") val id: Int = 0,
    @JsonNames("count", "Count", "ItemCount", "itemCount") val count: Int = 0,
)
