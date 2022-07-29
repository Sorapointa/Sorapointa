package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val weaponCurveLoader =
    DataLoader<List<WeaponCurveData>>("./ExcelBinOutput/WeaponCurveExcelConfigData.json")

val weaponCurveData get() = weaponCurveLoader.data

@Serializable
data class WeaponCurveData(
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
