package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeaponPromoteData(
    @SerialName("WeaponPromoteId") val weaponPromoteId: Int,
    @SerialName("CostItems") val costItems: List<CostItem>,
    @SerialName("AddProps") val addProps: List<AddProp>,
    @SerialName("UnlockMaxLevel") val unlockMaxLevel: Int,
    @SerialName("PromoteLevel") val promoteLevel: Int,
    @SerialName("RequiredPlayerLevel") val requiredPlayerLevel: Int,
    @SerialName("CoinCost") val coinCost: Int
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
