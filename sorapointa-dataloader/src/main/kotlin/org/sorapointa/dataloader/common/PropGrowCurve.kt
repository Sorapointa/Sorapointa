package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class PropGrowCurve(
    @JsonNames("type", "Type") val type: String,
    @JsonNames("growCurve", "GrowCurve") val growCurve: String,
)
