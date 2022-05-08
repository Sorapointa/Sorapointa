package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.common.FightPropData

@Serializable
data class AvatarTalentData(
    @SerialName("TalentId") val TalentId: Int,
    @SerialName("PrevTalent") val PrevTalent: Int,
    @SerialName("NameTextMapHash") val NameTextMapHash: Long,
    @SerialName("Icon") val Icon: String,
    @SerialName("MainCostItemId") val MainCostItemId: Int,
    @SerialName("MainCostItemCount") val MainCostItemCount: Int,
    @SerialName("OpenConfig") val OpenConfig: String,
    @SerialName("AddProps") val AddProps: List<FightPropData>, // Array
    @SerialName("ParamList") val ParamList: List<Float>, // Array
)


