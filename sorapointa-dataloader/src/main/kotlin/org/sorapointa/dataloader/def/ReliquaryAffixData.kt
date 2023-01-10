package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.FightProp
import org.sorapointa.dataloader.common.acceptEnum

private val reliquaryAffixLoader =
    DataLoader<List<ReliquaryAffixData>>("./ExcelBinOutput/ReliquaryAffixExcelConfigData.json")

val reliquaryAffix get() = reliquaryAffixLoader.data

@Serializable
data class ReliquaryAffixData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("depotId", "DepotId")
    val depotId: Int,
    @JsonNames("groupId", "GroupId")
    val groupId: Int,
    @JsonNames("propType", "PropType")
    private val _propType: JsonPrimitive,
    @JsonNames("propValue", "PropValue")
    val propValue: Double,
    @JsonNames("weight", "Weight")
    val weight: Int? = null,
    @JsonNames("upgradeWeight", "UpgradeWeight")
    val upgradeWeight: Int? = null
) {

    val propType by lazy {
        acceptEnum(_propType, FightProp.FIGHT_PROP_NONE)
    }
}
