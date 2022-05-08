package org.sorapointa.dataloader.custom

import kotlinx.serialization.SerialName

// TODO: 2022/5/8 ?
@kotlinx.serialization.Serializable
data class AbilityEmbryoEntry(
    @SerialName("name") val name: String,
    @SerialName("abilities") val abilities: List<String>, // Array
)
