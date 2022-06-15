package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val reliquarySetLoader =
    DataLoader<List<ReliquarySetData>>("./ExcelBinOutput/ReliquarySetExcelConfigData.json")

val reliquarySetData get() = reliquarySetLoader.data

@Serializable
data class ReliquarySetData(
    @JsonNames("setId", "SetId")
    val setId: Int,
    @JsonNames("setIcon", "SetIcon")
    val setIcon: String,
    @JsonNames("setNeedNum", "SetNeedNum")
    val setNeedNum: List<Int>,
    @JsonNames("equipAffixId", "EquipAffixId")
    val equipAffixId: Int,
    @JsonNames("containsList", "ContainsList")
    val containsList: List<Int>,
    @JsonNames("disableFilter", "DisableFilter")
    val disableFilter: Int
)
