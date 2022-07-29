package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.*

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
    val noFirstGetHint: Boolean = false,
    @JsonNames("useParam", "UseParam")
    val useParam: List<String> = listOf(),
    @JsonNames("rankLevel", "RankLevel")
    override val rankLevel: Int = 0,
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
    override val rank: Int = 0,
    @JsonNames("effectGadgetID", "EffectGadgetID")
    val effectGadgetID: Int? = null,
    @JsonNames("materialType", "MaterialType")
    val materialType: MaterialType = MaterialType.MATERIAL_NONE,
    @JsonNames("gadgetId", "GadgetId")
    override val gadgetId: Int = 0,
    @JsonNames("playGainEffect", "PlayGainEffect")
    val playGainEffect: Boolean = false,
    @JsonNames("stackLimit", "StackLimit")
    val stackLimit: Int? = null,
    @JsonNames("maxUseCount", "MaxUseCount")
    val maxUseCount: Int? = null,
    @JsonNames("closeBagAfterUsed", "CloseBagAfterUsed")
    val closeBagAfterUsed: Boolean = false,
    @JsonNames("useTarget", "UseTarget")
    val useTarget: ItemUseTarget = ItemUseTarget.ITEM_USE_TARGET_NONE,
    @JsonNames("useLevel", "UseLevel")
    val useLevel: Int? = null,
    @JsonNames("destroyRule", "DestroyRule")
    val destroyRule: MaterialDestroyType = MaterialDestroyType.DESTROY_NONE,
    @JsonNames("weight", "Weight")
    override val weight: Int = 0,
    @JsonNames("isForceGetHint", "IsForceGetHint")
    val isForceGetHint: Boolean = false,
    @JsonNames("foodQuality", "FoodQuality")
    val foodQuality: FoodQualityType = FoodQualityType.FOOD_QUALITY_NONE,
    @JsonNames("cdTime", "CdTime")
    val cdTime: Int? = null,
    @JsonNames("cdGroup", "CdGroup")
    val cdGroup: Int? = null,
    @JsonNames("isSplitDrop", "IsSplitDrop")
    val isSplitDrop: Boolean = false
) : ItemExcelData()
