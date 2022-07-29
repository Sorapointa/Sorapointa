package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.ItemParamData

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
    val coinCost: Int = 0,
    @JsonNames("costItems", "CostItems")
    private val _costItems: List<ItemParamData>,
    @JsonNames("filterConds", "FilterConds")
    val filterConds: List<String>,
    @JsonNames("breakLevel", "BreakLevel")
    val breakLevel: Int = 0,
    @JsonNames("paramDescList", "ParamDescList")
    val paramDescList: List<Long>,
    @JsonNames("lifeEffectParams", "LifeEffectParams")
    val lifeEffectParams: List<String>,
    @JsonNames("openConfig", "OpenConfig")
    val openConfig: String,
    @JsonNames("paramList", "ParamList")
    val paramList: List<Double>
) {

    val costItems by lazy {
        _costItems.filter { it.id != 0 }
    }
}
