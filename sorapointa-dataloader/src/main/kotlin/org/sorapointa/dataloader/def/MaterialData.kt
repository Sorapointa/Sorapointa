package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val materialDataLoader =
    DataLoader<List<MaterialData>>("./ExcelBinOutput/GadgetExcelConfigData.json")

val materialData get() = materialDataLoader.data

@Serializable
data class MaterialData(
    @JsonNames("interactionTitleTextMapHash", "InteractionTitleTextMapHash")
    val interactionTitleTextMapHash: Long,
    @JsonNames("noFirstGetHint", "NoFirstGetHint")
    val noFirstGetHint: Boolean,
    @JsonNames("useParam", "UseParam")
    val useParam: List<String>,
    @JsonNames("rankLevel", "RankLevel")
    val rankLevel: Int,
    @JsonNames("effectDescTextMapHash", "EffectDescTextMapHash")
    val effectDescTextMapHash: Long,
    @JsonNames("specialDescTextMapHash", "SpecialDescTextMapHash")
    val specialDescTextMapHash: Long,
    @JsonNames("typeDescTextMapHash", "TypeDescTextMapHash")
    val typeDescTextMapHash: Long,
    @JsonNames("effectIcon", "EffectIcon")
    val effectIcon: String,
    @JsonNames("effectName", "EffectName")
    val effectName: String,
    @JsonNames("satiationParams", "SatiationParams")
    val satiationParams: List<Int>,
//    Maybe has no data in json.
//    @JsonNames("destroyReturnMaterial", "DestroyReturnMaterial")
//    val destroyReturnMaterial: List<Any>,
//    @JsonNames("destroyReturnMaterialCount", "DestroyReturnMaterialCount")
//    val destroyReturnMaterialCount: List<Any>,
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
    @JsonNames("rank", "Rank")
    val rank: Int,
    @JsonNames("effectGadgetID", "EffectGadgetID")
    val effectGadgetID: Int,
    @JsonNames("materialType", "MaterialType")
    val materialType: String,
    @JsonNames("gadgetId", "GadgetId")
    val gadgetId: Int,
    @JsonNames("playGainEffect", "PlayGainEffect")
    val playGainEffect: Boolean,
    @JsonNames("stackLimit", "StackLimit")
    val stackLimit: Int,
    @JsonNames("maxUseCount", "MaxUseCount")
    val maxUseCount: Int,
    @JsonNames("closeBagAfterUsed", "CloseBagAfterUsed")
    val closeBagAfterUsed: Boolean,
    @JsonNames("useTarget", "UseTarget")
    val useTarget: String,
    @JsonNames("useLevel", "UseLevel")
    val useLevel: Int,
    @JsonNames("destroyRule", "DestroyRule")
    val destroyRule: String,
    @JsonNames("weight", "Weight")
    val weight: Int,
    @JsonNames("isForceGetHint", "IsForceGetHint")
    val isForceGetHint: Boolean,
    @JsonNames("foodQuality", "FoodQuality")
    val foodQuality: String,
    @JsonNames("cdTime", "CdTime")
    val cdTime: Int,
    @JsonNames("cdGroup", "CdGroup")
    val cdGroup: Int,
    @JsonNames("isSplitDrop", "IsSplitDrop")
    val isSplitDrop: Boolean
)
