package org.sorapointa.dataloader.common

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CurveInfo(
    @SerialName("Type") val type: String,
    @SerialName("Arith") val arith: String,
    @SerialName("Value") val value: Float,
)
