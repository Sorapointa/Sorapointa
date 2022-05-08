package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.common.FightPropData
import org.sorapointa.dataloader.common.ItemParamData

@Serializable
data class AvatarPromoteData(
    @SerialName("AvatarPromoteId") val avatarPromoteId: Int,
    @SerialName("PromoteLevel") val PromoteLevel: Int,
    @SerialName("ScoinCost") val scoinCost: Int,
    @SerialName("CostItems") val costItems: List<ItemParamData>, // Array
    @SerialName("UnlockMaxLevel") val unlockMaxLevel: Int,
    @SerialName("AddProps") val addProps: List<FightPropData>, // Array
    @SerialName("RequiredPlayerLevel") val requiredPlayerLevel: Int,
)
