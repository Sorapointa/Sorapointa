@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.EntityType

private val gadgetDataLoader =
    DataLoader<List<GadgetData>>("./ExcelBinOutput/GadgetExcelConfigData.json")

val gadgetData get() = gadgetDataLoader.data

@Serializable
data class GadgetData(
    @JsonNames("campID", "CampID")
    val campID: Int? = null,
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("inteeIconName", "InteeIconName")
    val inteeIconName: String,
    @JsonNames("interactNameTextMapHash", "InteractNameTextMapHash")
    val interactNameTextMapHash: Long,
    @JsonNames("itemJsonName", "ItemJsonName")
    val itemJsonName: String,
    @JsonNames("jsonName", "JsonName")
    val jsonName: String,
    @JsonNames("lODPatternName", "LODPatternName")
    val lODPatternName: String,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("prefabPathHashPre", "PrefabPathHashPre")
    val prefabPathHashPre: Int? = null,
    @JsonNames("prefabPathHashSuffix", "PrefabPathHashSuffix")
    val prefabPathHashSuffix: Long? = null,
    @JsonNames("tags", "Tags")
    val tags: List<String>,
    @JsonNames("type", "Type")
    val type: EntityType = EntityType.None
)
