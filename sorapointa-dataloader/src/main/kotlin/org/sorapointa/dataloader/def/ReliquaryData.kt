package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.ItemType
import org.sorapointa.dataloader.common.MaterialDestroyType
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
    val equipType: String,
    @JsonNames("showPic", "ShowPic")
    val showPic: String,
    @JsonNames("rankLevel", "RankLevel")
    override val rankLevel: Int,
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
    override val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("icon", "Icon")
    val icon: String,
    @JsonNames("itemType", "ItemType")
    override val itemType: ItemType,
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
    val destroyRule: MaterialDestroyType = MaterialDestroyType.DESTROY_NONE,
    @JsonNames("dropable", "Dropable")
    val dropable: Boolean = false,
) : ItemExcelData() {

    val mainProp by lazy {
        reliquaryMainPropData.firstOrNull { it.propDepotId == mainPropDepotId }
            ?: error("Couldn't find reliquaryMainDepotId:$mainPropDepotId data")
    }

    val appendProps by lazy {
        reliquaryAffix.filter { it.depotId == appendPropDepotId }
    }

    private val appendPropsWeightedMap by lazy {
        appendProps.associateWith { it.weight }
    }

    fun getRandomAppendProps() =
        appendPropsWeightedMap.weightRandom()
}
