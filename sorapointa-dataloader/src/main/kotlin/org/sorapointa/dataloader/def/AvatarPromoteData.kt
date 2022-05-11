package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AvatarPromoteDataItem(
    @SerialName("AvatarPromoteId") val avatarPromoteId: Int,
    @SerialName("PromoteAudio") val promoteAudio: String,
    @SerialName("CostItems") val costItems: List<CostItem>,
    @SerialName("UnlockMaxLevel") val unlockMaxLevel: Int,
    @SerialName("AddProps") val addProps: List<AddProp>,
    @SerialName("PromoteLevel") val promoteLevel: Int,
    @SerialName("ScoinCost") val scoinCost: Int,
    @SerialName("RequiredPlayerLevel") val requiredPlayerLevel: Int
) {
    @Serializable
    data class CostItem(
        @SerialName("Id") val id: Int,
        @SerialName("Count") val count: Int
    )

    @Serializable
    data class AddProp(
        @SerialName("PropType") val propType: String,
        @SerialName("Value") val value: Double
    )
}
