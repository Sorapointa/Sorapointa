package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ReliquaryLevelData(
    val id: Int,
    val propMap: Map<Int, Float>,
    @SerialName("Rank") val rank: Int,
    @SerialName("Level") val level: Int,
    @SerialName("Exp") val exp: Int,
    @SerialName("AddProps") val addProps: List<RelicLevelProperty>,
) {
    @kotlinx.serialization.Serializable
    data class RelicLevelProperty(
        @SerialName("PropType") val propType: String,
        @SerialName("Value") val value: Float,
    )
}
