package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AvatarTalentData(
    @SerialName("TalentId") val talentId: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("DescTextMapHash") val descTextMapHash: Long,
    @SerialName("Icon") val icon: String,
    @SerialName("PrevTalent") val prevTalent: Int,
    @SerialName("MainCostItemId") val mainCostItemId: Int,
    @SerialName("MainCostItemCount") val mainCostItemCount: Int,
    @SerialName("OpenConfig") val openConfig: String,
    @SerialName("AddProps") val addProps: List<AddProp>,
    @SerialName("ParamList") val paramList: List<Double>
) {
    @Serializable
    data class AddProp(
        @SerialName("PropType") val propType: String,
        @SerialName("Value") val value: Double
    )
}


