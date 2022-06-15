@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val equipAffixDataLoader =
    DataLoader<List<EquipAffixData>>("./ExcelBinOutput/EquipAffixExcelConfigData.json")

val equipAffixData get() = equipAffixDataLoader.data

@Serializable
data class EquipAffixData(
    @JsonNames("affixId", "AffixId")
    val affixId: Int,
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("openConfig", "OpenConfig")
    val openConfig: String,
    @JsonNames("addProps", "AddProps")
    val addProps: List<AddProp>,
    @JsonNames("paramList", "ParamList")
    val paramList: List<Double>
) {
    @Serializable
    data class AddProp(
        @JsonNames("propType", "PropType")
        val propType: String,
        @JsonNames("value", "Value")
        val value: Double
    )
}
