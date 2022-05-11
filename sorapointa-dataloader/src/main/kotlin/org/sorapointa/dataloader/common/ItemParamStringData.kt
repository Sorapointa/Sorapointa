package org.sorapointa.dataloader.common

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ItemParamStringData(
    @SerialName("Id") val id: Int,
    @SerialName("Count") val count: String,
)
