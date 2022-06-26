package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.ItemType

private val materialDataLoader =
    DataLoader<List<MaterialData>>("./ExcelBinOutput/MaterialExcelConfigData.json")

val materialData get() = materialDataLoader.data

val itemData by lazy {
   materialData + weaponData + reliquaryData
}

fun findItemExcelData(id: Int) =
    itemData.firstOrNull { it.id == id }

sealed class ItemExcelData {
    abstract val id: Int
    abstract val itemType: ItemType
    abstract val gadgetId: Int
    abstract val weight: Int
    abstract val rankLevel: Int
    abstract val rank: Int
}

@Serializable
data class MaterialData(
    @JsonNames("interactionTitleTextMapHash", "InteractionTitleTextMapHash")
    val interactionTitleTextMapHash: Long,
    @JsonNames("noFirstGetHint", "NoFirstGetHint")
    val noFirstGetHint: Boolean,
    @JsonNames("useParam", "UseParam")
    val useParam: List<String>,
    @JsonNames("rankLevel", "RankLevel")
    override val rankLevel: Int,
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
//    Maybe there is no data in json.
//    @JsonNames("destroyReturnMaterial", "DestroyReturnMaterial")
//    val destroyReturnMaterial: List<Any>,
//    @JsonNames("destroyReturnMaterialCount", "DestroyReturnMaterialCount")
//    val destroyReturnMaterialCount: List<Any>,
    @JsonNames("id", "Id")
    override val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("icon", "Icon")
    val icon: String,
    @JsonNames("itemType", "ItemType")
    override val itemType: ItemType,
    @JsonNames("rank", "Rank")
    override val rank: Int,
    @JsonNames("effectGadgetID", "EffectGadgetID")
    val effectGadgetID: Int,
    @JsonNames("materialType", "MaterialType")
    val materialType: String,
    @JsonNames("gadgetId", "GadgetId")
    override val gadgetId: Int,
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
    override val weight: Int,
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
) : ItemExcelData()
