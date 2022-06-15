package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val weaponPromoteLoader =
    DataLoader<List<WeaponPromoteData>>("./ExcelBinOutput/WeaponPromoteExcelConfigData.json")

val weaponPromoteData get() = weaponPromoteLoader.data

@Serializable
data class WeaponPromoteData(
    @JsonNames("weaponPromoteId", "WeaponPromoteId")
    val weaponPromoteId: Int,
    @JsonNames("costItems", "CostItems")
    val costItems: List<CostItem>,
    @JsonNames("addProps", "AddProps")
    val addProps: List<AddProp>,
    @JsonNames("unlockMaxLevel", "UnlockMaxLevel")
    val unlockMaxLevel: Int,
    @JsonNames("promoteLevel", "PromoteLevel")
    val promoteLevel: Int,
    @JsonNames("requiredPlayerLevel", "RequiredPlayerLevel")
    val requiredPlayerLevel: Int,
    @JsonNames("coinCost", "CoinCost")
    val coinCost: Int
) {
    @Serializable
    data class CostItem(
        @JsonNames("id", "Id")
        val id: Int,
        @JsonNames("count", "Count")
        val count: Int
    )

    @Serializable
    data class AddProp(
        @JsonNames("propType", "PropType")
        val propType: String,
        @JsonNames("value", "Value")
        val value: Double
    )
}
