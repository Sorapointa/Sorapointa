package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReliquaryData(
    @SerialName("EquipType") val equipType: String,
    @SerialName("ShowPic") val showPic: String,
    @SerialName("RankLevel") val rankLevel: Int,
    @SerialName("MainPropDepotId") val mainPropDepotId: Int,
    @SerialName("AppendPropDepotId") val appendPropDepotId: Int,
    @SerialName("AddPropLevels") val addPropLevels: List<Int>,
    @SerialName("BaseConvExp") val baseConvExp: Int,
    @SerialName("MaxLevel") val maxLevel: Int,
    @SerialName("DestroyReturnMaterial") val destroyReturnMaterial: List<Int>,
    @SerialName("DestroyReturnMaterialCount") val destroyReturnMaterialCount: List<Int>,
    @SerialName("Id") val id: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("DescTextMapHash") val descTextMapHash: Long,
    @SerialName("Icon") val icon: String,
    @SerialName("ItemType") val itemType: String,
    @SerialName("Weight") val weight: Int,
    @SerialName("Rank") val rank: Int,
    @SerialName("GadgetId") val gadgetId: Int,
    @SerialName("AppendPropNum") val appendPropNum: Int,
    @SerialName("SetId") val setId: Int,
    @SerialName("StoryId") val storyId: Int,
    @SerialName("DestroyRule") val destroyRule: String,
    @SerialName("Dropable") val dropable: Boolean
)
