package org.sorapointa.dataloader.common

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class PropGrowCurve(
    @SerialName("Type") val type: String,
    @SerialName("GrowCurve") val growCurve: String,
)
