package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProudSkillData(
    @SerialName("ProudSkillId") val proudSkillId: Int,
    @SerialName("ProudSkillGroupId") val proudSkillGroupId: Int,
    @SerialName("Level") val level: Int,
    @SerialName("ProudSkillType") val proudSkillType: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("DescTextMapHash") val descTextMapHash: Long,
    @SerialName("UnlockDescTextMapHash") val unlockDescTextMapHash: Long,
    @SerialName("Icon") val icon: String,
    @SerialName("CoinCost") val coinCost: Int,
    @SerialName("CostItems") val costItems: List<CostItem>,
    @SerialName("FilterConds") val filterConds: List<String>,
    @SerialName("BreakLevel") val breakLevel: Int,
    @SerialName("ParamDescList") val paramDescList: List<Long>,
    @SerialName("LifeEffectParams") val lifeEffectParams: List<String>,
    @SerialName("OpenConfig") val openConfig: String,
    @SerialName("AddProps") val addProps: List<AddProp>,
    @SerialName("ParamList") val paramList: List<Double>
) {
    @Serializable
    data class CostItem(
        @SerialName("Id") val id: Int,
        @SerialName("Count") val count: Int
    )

    @Serializable
    class AddProp
}
