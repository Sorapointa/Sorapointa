package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.common.CurveInfo

@Serializable
data class MonsterCurveData(
    @SerialName("Level") val level: Int,
    @SerialName("CurveInfoList") val curveInfoList: List<CurveInfo>, // Array
    @SerialName("CurveInfoMap") val curveInfoMap: Map<String, Float>,
)
