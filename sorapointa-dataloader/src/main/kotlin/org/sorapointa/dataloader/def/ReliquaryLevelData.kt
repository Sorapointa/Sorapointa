package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.AddProp
import org.sorapointa.dataloader.common.FightProp

private val reliquaryLevelLoader =
    DataLoader<List<ReliquaryLevelData>>("./ExcelBinOutput/ReliquaryLevelExcelConfigData.json")

val reliquaryLevelData get() = reliquaryLevelLoader.data

@Serializable
data class ReliquaryLevelData(
    @JsonNames("level", "Level")
    val level: Int,
    @JsonNames("addProps", "AddProps")
    private val _addProps: List<AddProp>,
    @JsonNames("rank", "Rank")
    val rank: Int = 0,
    @JsonNames("exp", "Exp")
    val exp: Int = 0,
) {

    val addProp by lazy {
        _addProps.filter { it.propType != FightProp.FIGHT_PROP_NONE }
    }
}
