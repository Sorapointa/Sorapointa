package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val monsterCurveDataLoader =
    DataLoader<List<MonsterCurveData>>("./ExcelBinOutput/MonsterCurveExcelConfigData.json")

val monsterCurveData get() = monsterCurveDataLoader.data

@Serializable
data class MonsterCurveData(
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
