package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.FightProp
import org.sorapointa.dataloader.common.acceptEnum

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
    private val _propType: JsonPrimitive,
    @JsonNames("weight", "Weight")
    val weight: Int? = null,
) {

    val propType by lazy {
        acceptEnum(_propType, FightProp.FIGHT_PROP_NONE)
    }
}
