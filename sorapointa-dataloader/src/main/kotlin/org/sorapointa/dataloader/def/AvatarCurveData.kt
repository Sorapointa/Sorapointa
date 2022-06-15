@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val avatarCurveDataLoader =
    DataLoader<List<AvatarCurveData>>("./ExcelBinOutput/AvatarCurveExcelConfigData.json")

val avatarCurveDataList get() = avatarCurveDataLoader.data

@Serializable
data class AvatarCurveData(
    @JsonNames("level", "Level")
    val level: Int,
    @JsonNames("curveInfos", "CurveInfos")
    val curveInfos: List<CurveInfo>
) {
    @Serializable
    data class CurveInfo(
        @JsonNames("type", "Type")
        val type: String,
        @JsonNames("arith", "Arith")
        val arith: String,
        @JsonNames("value", "Value")
        val value: Double
    )
}
