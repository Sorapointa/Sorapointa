package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val proudSkillLoader =
    DataLoader<List<ProudSkillData>>("./ExcelBinOutput/ProudSkillExcelConfigData.json")

val proudSkillData get() = proudSkillLoader.data

@Serializable
data class ProudSkillData(
    @JsonNames("proudSkillId", "ProudSkillId")
    val proudSkillId: Int,
    @JsonNames("proudSkillGroupId", "ProudSkillGroupId")
    val proudSkillGroupId: Int,
    @JsonNames("level", "Level")
    val level: Int,
    @JsonNames("proudSkillType", "ProudSkillType")
    val proudSkillType: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("unlockDescTextMapHash", "UnlockDescTextMapHash")
    val unlockDescTextMapHash: Long,
    @JsonNames("icon", "Icon")
    val icon: String,
    @JsonNames("coinCost", "CoinCost")
    val coinCost: Int,
    @JsonNames("costItems", "CostItems")
    val costItems: List<CostItem>,
    @JsonNames("filterConds", "FilterConds")
    val filterConds: List<String>,
    @JsonNames("breakLevel", "BreakLevel")
    val breakLevel: Int,
    @JsonNames("paramDescList", "ParamDescList")
    val paramDescList: List<Long>,
    @JsonNames("lifeEffectParams", "LifeEffectParams")
    val lifeEffectParams: List<String>,
    @JsonNames("openConfig", "OpenConfig")
    val openConfig: String,
    @JsonNames("addProps", "AddProps")
    val addProps: List<AddProp>,
    @JsonNames("paramList", "ParamList")
    val paramList: List<Double>
) {
    @Serializable
    data class CostItem(
        @JsonNames("id", "Id")
        val id: Int,
        @JsonNames("count", "Count")
        val count: Int
    )

    @Serializable
    class AddProp
}
