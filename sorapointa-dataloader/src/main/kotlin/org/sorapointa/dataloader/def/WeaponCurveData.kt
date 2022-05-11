package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class WeaponCurveDataItem(
    @SerialName("Level") val level: Int,
    @SerialName("CurveInfos") val curveInfos: List<CurveInfo>
) {
    @Serializable
    data class CurveInfo(
        @SerialName("Type") val type: String,
        @SerialName("Arith") val arith: String,
        @SerialName("Value") val value: Double
    )
}

