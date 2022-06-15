@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val combineDataLoader =
    DataLoader<List<CombineData>>("./ExcelBinOutput/CombineExcelConfigData.json")
val combineData get() = combineDataLoader.data

@Serializable
data class CombineData(
    @JsonNames("combineId", "CombineId")
    val combineId: Int,
    @JsonNames("playerLevel", "PlayerLevel")
    val playerLevel: Int,
    @JsonNames("isDefaultShow", "IsDefaultShow")
    val isDefaultShow: Boolean,
    @JsonNames("combineType", "CombineType")
    val combineType: Int,
    @JsonNames("subCombineType", "SubCombineType")
    val subCombineType: Int,
    @JsonNames("resultItemId", "ResultItemId")
    val resultItemId: Int,
    @JsonNames("resultItemCount", "ResultItemCount")
    val resultItemCount: Int,
    @JsonNames("scoinCost", "ScoinCost")
    val scoinCost: Int,
    @JsonNames("randomItems", "RandomItems")
    val randomItems: List<RandomItem>,
    @JsonNames("materialItems", "MaterialItems")
    val materialItems: List<MaterialItem>,
    @JsonNames("effectDescTextMapHash", "EffectDescTextMapHash")
    val effectDescTextMapHash: Int,
    @JsonNames("recipeType", "RecipeType")
    val recipeType: String
) {
    @Serializable
    data class RandomItem(
        @JsonNames("count", "Count")
        val count: Int
    )

    @Serializable
    data class MaterialItem(
        @JsonNames("id", "Id")
        val id: Int,
        @JsonNames("count", "Count")
        val count: Int
    )
}
