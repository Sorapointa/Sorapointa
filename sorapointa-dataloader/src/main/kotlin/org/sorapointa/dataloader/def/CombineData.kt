package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CombineData(
    @SerialName("CombineId") val combineId: Int,
    @SerialName("PlayerLevel") val playerLevel: Int,
    @SerialName("IsDefaultShow") val isDefaultShow: Boolean,
    @SerialName("CombineType") val combineType: Int,
    @SerialName("SubCombineType") val subCombineType: Int,
    @SerialName("ResultItemId") val resultItemId: Int,
    @SerialName("ResultItemCount") val resultItemCount: Int,
    @SerialName("ScoinCost") val scoinCost: Int,
    @SerialName("RandomItems") val randomItems: List<RandomItem>,
    @SerialName("MaterialItems") val materialItems: List<MaterialItem>,
    @SerialName("EffectDescTextMapHash") val effectDescTextMapHash: Int,
    @SerialName("RecipeType") val recipeType: String
) {
    @Serializable
    data class RandomItem(
        @SerialName("Count") val count: Int
    )

    @Serializable
    data class MaterialItem(
        @SerialName("Id") val id: Int,
        @SerialName("Count") val count: Int
    )
}
