package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReliquaryLevelData(
    @SerialName("Level") val level: Int,
    @SerialName("AddProps") val addProps: List<AddProp>,
    @SerialName("Rank") val rank: Int,
    @SerialName("Exp") val exp: Int
) {
    @Serializable
    data class AddProp(
        @SerialName("PropType") val propType: String,
        @SerialName("Value") val value: Double
    )
}
