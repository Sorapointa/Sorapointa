package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

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
    val propType: String,
    @JsonNames("propValue", "PropValue")
    val propValue: Double,
    @JsonNames("weight", "Weight")
    val weight: Int,
    @JsonNames("upgradeWeight", "UpgradeWeight")
    val upgradeWeight: Int
)
