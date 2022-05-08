package org.sorapointa.dataloader.common

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ItemParamData(
    @SerialName("Id") val id: Int,
    @SerialName("Count") val count: Int,
)
