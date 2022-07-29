package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val reliquaryMainPropLoader =
    DataLoader<List<ReliquaryMainPropData>>("./ExcelBinOutput/ReliquaryMainPropExcelConfigData.json")

val reliquaryMainPropData get() = reliquaryMainPropLoader.data

@Serializable
data class ReliquaryMainPropData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("propDepotId", "PropDepotId")
    val propDepotId: Int,
    @JsonNames("propType", "PropType")
    val propType: String,
    @JsonNames("affixName", "AffixName")
    val affixName: String,
    @JsonNames("weight", "Weight")
    val weight: Int? = null,
)
