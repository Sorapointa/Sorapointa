package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class CurveInfo(
    @JsonNames("type", "Type") val type: String,
    @JsonNames("arith", "Arith") val arith: String,
    @JsonNames("value", "Value") val value: Float,
)
