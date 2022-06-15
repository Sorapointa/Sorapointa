package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val reliquaryLevelLoader =
    DataLoader<List<ReliquaryLevelData>>("./ExcelBinOutput/ReliquaryLevelExcelConfigData.json")

val reliquaryLevelData get() = reliquaryLevelLoader.data

@Serializable
data class ReliquaryLevelData(
    @JsonNames("level", "Level")
    val level: Int,
    @JsonNames("addProps", "AddProps")
    val addProps: List<AddProp>,
    @JsonNames("rank", "Rank")
    val rank: Int,
    @JsonNames("exp", "Exp")
    val exp: Int
) {
    @Serializable
    data class AddProp(
        @JsonNames("propType", "PropType")
        val propType: String,
        @JsonNames("value", "Value")
        val value: Double
    )
}
