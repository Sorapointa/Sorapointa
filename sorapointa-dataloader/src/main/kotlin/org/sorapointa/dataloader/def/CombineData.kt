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
    @SerialName("ScoinCost") val scoinCost: Int,
    @SerialName("RandomItems") val randomItems: List<CombineItemPair>,
    @SerialName("MaterialItems") val materialItems: List<CombineItemPair>,
    @SerialName("EffectDescTextMapHash") val effectDescTextMapHash: Long,
    @SerialName("RecipeType") val recipeType: String,
) {
    @Serializable
    data class CombineItemPair(
        @SerialName("Id") val id: Int,
        @SerialName("Count") val count: Int,
    )
}
