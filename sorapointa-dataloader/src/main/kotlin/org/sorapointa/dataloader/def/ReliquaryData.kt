package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val reliquaryLoader =
    DataLoader<List<ReliquaryData>>("./ExcelBinOutput/ReliquaryExcelConfigData.json")

val reliquaryData get() = reliquaryLoader.data

@Serializable
data class ReliquaryData(
    @JsonNames("equipType", "EquipType")
    val equipType: String,
    @JsonNames("showPic", "ShowPic")
    val showPic: String,
    @JsonNames("rankLevel", "RankLevel")
    val rankLevel: Int,
    @JsonNames("mainPropDepotId", "MainPropDepotId")
    val mainPropDepotId: Int,
    @JsonNames("appendPropDepotId", "AppendPropDepotId")
    val appendPropDepotId: Int,
    @JsonNames("addPropLevels", "AddPropLevels")
    val addPropLevels: List<Int>,
    @JsonNames("baseConvExp", "BaseConvExp")
    val baseConvExp: Int,
    @JsonNames("maxLevel", "MaxLevel")
    val maxLevel: Int,
    @JsonNames("destroyReturnMaterial", "DestroyReturnMaterial")
    val destroyReturnMaterial: List<Int>,
    @JsonNames("destroyReturnMaterialCount", "DestroyReturnMaterialCount")
    val destroyReturnMaterialCount: List<Int>,
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("icon", "Icon")
    val icon: String,
    @JsonNames("itemType", "ItemType")
    val itemType: String,
    @JsonNames("weight", "Weight")
    val weight: Int,
    @JsonNames("rank", "Rank")
    val rank: Int,
    @JsonNames("gadgetId", "GadgetId")
    val gadgetId: Int,
    @JsonNames("appendPropNum", "AppendPropNum")
    val appendPropNum: Int,
    @JsonNames("setId", "SetId")
    val setId: Int,
    @JsonNames("storyId", "StoryId")
    val storyId: Int,
    @JsonNames("destroyRule", "DestroyRule")
    val destroyRule: String,
    @JsonNames("dropable", "Dropable")
    val dropable: Boolean,
)
