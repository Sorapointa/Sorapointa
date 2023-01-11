package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive
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

interface ItemExcelData {
    val id: Int
    val itemType: ItemType
    val gadgetId: Int
    val weight: Int
    val rankLevel: Int
    val rank: Int
}

@Serializable
data class MaterialData(
    @JsonNames("interactionTitleTextMapHash", "InteractionTitleTextMapHash")
    val interactionTitleTextMapHash: ULong,
    @JsonNames("noFirstGetHint", "NoFirstGetHint")
    val noFirstGetHint: Boolean = false,
    @JsonNames("useParam", "UseParam")
    val useParam: List<String> = listOf(),
    @JsonNames("rankLevel", "RankLevel")
    override val rankLevel: Int = 0,
    @JsonNames("effectDescTextMapHash", "EffectDescTextMapHash")
    val effectDescTextMapHash: ULong,
    @JsonNames("specialDescTextMapHash", "SpecialDescTextMapHash")
    val specialDescTextMapHash: ULong,
    @JsonNames("typeDescTextMapHash", "TypeDescTextMapHash")
    val typeDescTextMapHash: ULong,
    @JsonNames("effectIcon", "EffectIcon")
    val effectIcon: String? = null,
    @JsonNames("effectName", "EffectName")
    val effectName: String? = null,
    @JsonNames("satiationParams", "SatiationParams")
    val satiationParams: List<Int>? = null,
    @JsonNames("id", "Id")
    override val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: ULong,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: ULong,
    @JsonNames("icon", "Icon")
    val icon: String? = null,
    @JsonNames("itemType", "ItemType")
    private val _itemType: JsonPrimitive,
    @JsonNames("rank", "Rank")
    override val rank: Int = 0,
    @JsonNames("effectGadgetID", "EffectGadgetID")
    val effectGadgetID: Int? = null,
    @JsonNames("materialType", "MaterialType")
    private val _materialType: JsonPrimitive? = null,
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
    @JsonNames("useLevel", "UseLevel")
    val useLevel: Int? = null,
    @JsonNames("destroyRule", "DestroyRule")
    private val _destroyRule: JsonPrimitive? = null,
    @JsonNames("weight", "Weight")
    override val weight: Int = 0,
    @JsonNames("isForceGetHint", "IsForceGetHint")
    val isForceGetHint: Boolean = false,
    @JsonNames("foodQuality", "FoodQuality")
    private val foodQuality: JsonPrimitive? = null,
    @JsonNames("cdTime", "CdTime")
    val cdTime: Int? = null,
    @JsonNames("cdGroup", "CdGroup")
    val cdGroup: Int? = null,
    @JsonNames("isSplitDrop", "IsSplitDrop")
    val isSplitDrop: Boolean = false,
) : ItemExcelData {

    override val itemType by lazy {
        acceptEnum(_itemType, ItemType.ITEM_NONE)
    }

    val materialType by lazy {
        _materialType?.let {
            acceptEnum(it, MaterialType.MATERIAL_NONE)
        }
    }

    val destroyRule by lazy {
        _destroyRule?.let {
            acceptEnum(it, MaterialDestroyType.DESTROY_NONE)
        }
    }

    val foodQualityType by lazy {
        foodQuality?.let {
            acceptEnum(it, FoodQualityType.FOOD_QUALITY_NONE)
        }
    }
}
