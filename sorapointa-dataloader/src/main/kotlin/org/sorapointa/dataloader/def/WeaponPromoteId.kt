package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import org.sorapointa.dataloader.common.FightPropData
import org.sorapointa.dataloader.common.ItemParamData

@kotlinx.serialization.Serializable
data class WeaponPromoteId(
    @SerialName("WeaponPromoteId") val weaponPromoteId: Int,
    @SerialName("PromoteLevel") val promoteLevel: Int,
    @SerialName("CostItems") val costItems: List<ItemParamData>, // Array
    @SerialName("CoinCost") val coinCost: Int,
    @SerialName("AddProps") val addProps: List<FightPropData>, // Array
    @SerialName("UnlockMaxLevel") val unlockMaxLevel: Int,
    @SerialName("RequiredPlayerLevel") val requiredPlayerLevel: Int,
)
