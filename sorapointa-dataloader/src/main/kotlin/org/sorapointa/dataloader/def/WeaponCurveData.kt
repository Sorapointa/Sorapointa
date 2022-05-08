package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import org.sorapointa.dataloader.common.CurveInfo

@kotlinx.serialization.Serializable
data class WeaponCurveData(
    @SerialName("Level") val level: Int,
    @SerialName("CurveInfos") val curveInfos: List<CurveInfo>, // Array
    val curveInfoMap: Map<String, Float>,
)
