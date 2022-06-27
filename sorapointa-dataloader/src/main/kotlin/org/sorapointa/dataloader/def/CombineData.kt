@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.ItemParamData
import org.sorapointa.dataloader.common.RecipeType

private val combineDataLoader =
    DataLoader<List<CombineData>>("./ExcelBinOutput/CombineExcelConfigData.json")
val combineData get() = combineDataLoader.data

@Serializable
data class CombineData(
    @JsonNames("combineId", "CombineId")
    val combineId: Int,
    @JsonNames("playerLevel", "PlayerLevel")
    val playerLevel: Int = 0,
    @JsonNames("isDefaultShow", "IsDefaultShow")
    val isDefaultShow: Boolean = false,
    @JsonNames("combineType", "CombineType")
    val combineType: Int,
    @JsonNames("subCombineType", "SubCombineType")
    val subCombineType: Int,
    @JsonNames("resultItemId", "ResultItemId")
    val resultItemId: Int,
    @JsonNames("resultItemCount", "ResultItemCount")
    val resultItemCount: Int,
    @JsonNames("scoinCost", "ScoinCost")
    val scoinCost: Int = 0,
    @JsonNames("materialItems", "MaterialItems")
    private val _materialItems: List<ItemParamData>,
    @JsonNames("effectDescTextMapHash", "EffectDescTextMapHash")
    val effectDescTextMapHash: Long,
    @JsonNames("recipeType", "RecipeType")
    val recipeType: RecipeType = RecipeType.RECIPE_TYPE_NONE
) {

    val materialItems by lazy {
        _materialItems.filter { it.id != 0 }
    }

    @Serializable
    data class RandomItem(
        @JsonNames("count", "Count")
        val count: Int
    )
}
