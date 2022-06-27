package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.AddProp
import org.sorapointa.dataloader.common.FightProp
import org.sorapointa.dataloader.common.ItemParamData

private val weaponPromoteLoader =
    DataLoader<List<WeaponPromoteData>>("./ExcelBinOutput/WeaponPromoteExcelConfigData.json")

val weaponPromoteData get() = weaponPromoteLoader.data

@Serializable
data class WeaponPromoteData(
    @JsonNames("weaponPromoteId", "WeaponPromoteId")
    val weaponPromoteId: Int,
    @JsonNames("costItems", "CostItems")
    private val _costItems: List<ItemParamData>,
    @JsonNames("addProps", "AddProps")
    private val _addProps: List<AddProp>,
    @JsonNames("unlockMaxLevel", "UnlockMaxLevel")
    val unlockMaxLevel: Int,
    @JsonNames("promoteLevel", "PromoteLevel")
    val promoteLevel: Int = 0,
    @JsonNames("requiredPlayerLevel", "RequiredPlayerLevel")
    val requiredPlayerLevel: Int = 0,
    @JsonNames("coinCost", "CoinCost")
    val coinCost: Int = 0
) {

    val costItems by lazy {
        _costItems.filter { it.id != 0 }
    }

    val addProp by lazy {
        _addProps.filter { it.propType != FightProp.FIGHT_PROP_NONE }
    }
}
