package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.EquipType
import org.sorapointa.dataloader.common.ItemType
import org.sorapointa.dataloader.common.MaterialDestroyType
import org.sorapointa.dataloader.common.acceptEnum
import org.sorapointa.utils.weightRandom

private val reliquaryLoader =
    DataLoader<List<ReliquaryData>>("./ExcelBinOutput/ReliquaryExcelConfigData.json")

val reliquaryData get() = reliquaryLoader.data

fun findReliquaryExcelData(id: Int) =
    reliquaryData.firstOrNull { it.id == id }
        ?: error("Couldn't find reliquaryId:$id reliquary data")

@Serializable
data class ReliquaryData(
    @JsonNames("equipType", "EquipType")
    private val _equipType: JsonPrimitive,
    @JsonNames("showPic", "ShowPic")
    val showPic: String,
    @JsonNames("rankLevel", "RankLevel")
    override val rankLevel: Int,
    @JsonNames("mainPropDepotId", "MainPropDepotId")
    val mainPropDepotId: Int,
    @JsonNames("appendPropDepotId", "AppendPropDepotId")
    val appendPropDepotId: Int,
    @JsonNames("addPropLevels", "AddPropLevels")
    val addPropLevels: List<Int>? = null,
    @JsonNames("baseConvExp", "BaseConvExp")
    val baseConvExp: Int,
    @JsonNames("maxLevel", "MaxLevel")
    val maxLevel: Int,
    @JsonNames("destroyReturnMaterial", "DestroyReturnMaterial")
    val destroyReturnMaterial: List<Int>? = null,
    @JsonNames("destroyReturnMaterialCount", "DestroyReturnMaterialCount")
    val destroyReturnMaterialCount: List<Int>? = null,
    @JsonNames("id", "Id")
    override val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: ULong,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: ULong,
    @JsonNames("icon", "Icon")
    val icon: String,
    @JsonNames("itemType", "ItemType")
    private val _itemType: JsonPrimitive,
    @JsonNames("weight", "Weight")
    override val weight: Int,
    @JsonNames("rank", "Rank")
    override val rank: Int,
    @JsonNames("gadgetId", "GadgetId")
    override val gadgetId: Int,
    @JsonNames("appendPropNum", "AppendPropNum")
    val appendPropNum: Int = 0,
    @JsonNames("setId", "SetId")
    val setId: Int = 0,
    @JsonNames("storyId", "StoryId")
    val storyId: Int = 0,
    @JsonNames("destroyRule", "DestroyRule")
    private val _destroyRule: JsonPrimitive? = null,
    @JsonNames("dropable", "Dropable")
    val dropable: Boolean = false,
) : ItemExcelData {

    val equipType by lazy {
        acceptEnum(_equipType, EquipType.EQUIP_NONE)
    }

    override val itemType by lazy {
        acceptEnum(_itemType, ItemType.ITEM_NONE)
    }

    val mainProp by lazy {
        reliquaryMainPropData.firstOrNull { it.propDepotId == mainPropDepotId }
            ?: error("Couldn't find reliquaryMainDepotId:$mainPropDepotId data")
    }

    val appendProps by lazy {
        reliquaryAffix.filter { it.depotId == appendPropDepotId }
    }

    private val appendPropsWeightedMap by lazy {
        appendProps.associateWith { it.weight ?: 0 }
    }

    val destroyRule by lazy {
        _destroyRule?.let {
            acceptEnum(_destroyRule, MaterialDestroyType.DESTROY_NONE)
        }
    }

    fun getRandomAppendProps() =
        appendPropsWeightedMap.weightRandom()
}
