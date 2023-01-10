package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val reliquarySetLoader =
    DataLoader<List<ReliquarySetData>>("./ExcelBinOutput/ReliquarySetExcelConfigData.json")

val reliquarySetData get() = reliquarySetLoader.data

@Serializable
data class ReliquarySetData(
    @JsonNames("setId", "SetId")
    val setId: Int,
    @JsonNames("setIcon", "SetIcon")
    val setIcon: String? = null,
    @JsonNames("setNeedNum", "SetNeedNum")
    val setNeedNum: List<Int>,
    @JsonNames("equipAffixId", "EquipAffixId")
    val equipAffixId: Int? = null,
    @JsonNames("containsList", "ContainsList")
    val containsList: List<Int>,
    @JsonNames("disableFilter", "DisableFilter")
    val disableFilter: Int? = null
)
