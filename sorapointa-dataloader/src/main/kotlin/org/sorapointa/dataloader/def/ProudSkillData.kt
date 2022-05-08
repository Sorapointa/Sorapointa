package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import org.sorapointa.dataloader.common.FightPropData
import org.sorapointa.dataloader.common.ItemParamData

@kotlinx.serialization.Serializable
data class ProudSkillData(
    @SerialName("ProudSkillId") val proudSkillId: Int,
    @SerialName("ProudSkillGroupId") val proudSkillGroupId: Int,
    @SerialName("Level") val level: Int,
    @SerialName("CoinCost") val coinCost: Int,
    @SerialName("BreakLevel") val breakLevel: Int,
    @SerialName("ProudSkillType") val proudSkillType: Int,
    @SerialName("OpenConfig") val openConfig: String,
    @SerialName("CostItems") val costItems: List<ItemParamData>,
    @SerialName("FilterConds") val filterConds: List<String>,
    @SerialName("LifeEffectParams") val lifeEffectParams: List<String>,
    @SerialName("AddProps") val addProps: List<FightPropData>, // Array
    @SerialName("ParamList") val paramList: List<Float>, // Array
    @SerialName("ParamDescList") val paramDescList: List<Long>, // Array
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
)
