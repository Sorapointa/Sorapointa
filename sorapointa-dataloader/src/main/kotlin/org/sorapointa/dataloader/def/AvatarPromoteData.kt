@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

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
    val costItems: List<CostItem>,
    @JsonNames("unlockMaxLevel", "UnlockMaxLevel")
    val unlockMaxLevel: Int,
    @JsonNames("addProps", "AddProps")
    val addProps: List<AddProp>,
    @JsonNames("promoteLevel", "PromoteLevel")
    val promoteLevel: Int,
    @JsonNames("scoinCost", "ScoinCost")
    val scoinCost: Int,
    @JsonNames("requiredPlayerLevel", "RequiredPlayerLevel")
    val requiredPlayerLevel: Int
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
