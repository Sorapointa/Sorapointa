package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.AddProp
import org.sorapointa.dataloader.common.FightProp
import org.sorapointa.dataloader.common.ItemParamData

private val avatarPromoteDataLoader =
    DataLoader<List<AvatarPromoteData>>("./ExcelBinOutput/AvatarPromoteExcelConfigData.json")

val avatarPromoteData get() = avatarPromoteDataLoader.data

@Serializable
data class AvatarPromoteData(
    @JsonNames("avatarPromoteId", "AvatarPromoteId")
    val avatarPromoteId: Int,
    @JsonNames("promoteAudio", "PromoteAudio")
    val promoteAudio: String,
    @JsonNames("costItems", "CostItems")
    private val _costItems: List<ItemParamData>,
    @JsonNames("unlockMaxLevel", "UnlockMaxLevel")
    val unlockMaxLevel: Int,
    @JsonNames("addProps", "AddProps")
    private val _addProps: List<AddProp>,
    @JsonNames("promoteLevel", "PromoteLevel")
    val promoteLevel: Int = 0,
    @JsonNames("scoinCost", "ScoinCost")
    val scoinCost: Int = 0,
    @JsonNames("requiredPlayerLevel", "RequiredPlayerLevel")
    val requiredPlayerLevel: Int = 0
) {

    val costItem by lazy {
        _costItems.filter { it.id != 0 }
    }

    val addProp by lazy {
        _addProps.filter { it.propType != FightProp.FIGHT_PROP_NONE }
    }
}
