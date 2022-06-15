@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val gadgetDataLoader =
    DataLoader<List<GadgetData>>("./ExcelBinOutput/GadgetExcelConfigData.json")

val gadgetData get() = gadgetDataLoader.data

@Serializable
data class GadgetData(
    @JsonNames("campID", "CampID")
    val campID: Int,
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("inteeIconName", "InteeIconName")
    val inteeIconName: String,
    @JsonNames("interactNameTextMapHash", "InteractNameTextMapHash")
    val interactNameTextMapHash: Int,
    @JsonNames("itemJsonName", "ItemJsonName")
    val itemJsonName: String,
    @JsonNames("jsonName", "JsonName")
    val jsonName: String,
    @JsonNames("lODPatternName", "LODPatternName")
    val lODPatternName: String,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("prefabPathHashPre", "PrefabPathHashPre")
    val prefabPathHashPre: Int,
    @JsonNames("prefabPathHashSuffix", "PrefabPathHashSuffix")
    val prefabPathHashSuffix: Int,
    @JsonNames("tags", "Tags")
    val tags: List<String>,
    @JsonNames("type", "Type")
    val type: String
)
