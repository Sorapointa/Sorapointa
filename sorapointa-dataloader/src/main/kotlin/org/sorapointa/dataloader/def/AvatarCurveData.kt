package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.common.CurveInfo

@Serializable
data class AvatarCurveData(
    @SerialName("Level") val level: Int,
    @SerialName("CurveInfos") val curveInfoList: List<CurveInfo>, // Array
)
