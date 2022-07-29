package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ItemParamStringData(
    @JsonNames("id", "Id") val id: Int,
    @JsonNames("count", "Count") val count: String,
)
