@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.AddProp
import org.sorapointa.dataloader.common.FightProp

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
    private val _addProps: List<AddProp>,
    @JsonNames("paramList", "ParamList")
    val paramList: List<Double>
) {

    val addProp by lazy {
        _addProps.filter { it.propType != FightProp.FIGHT_PROP_NONE }
    }
}
