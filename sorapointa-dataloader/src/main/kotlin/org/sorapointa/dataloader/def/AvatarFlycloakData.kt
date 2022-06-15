@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val avatarFlycloakDataLoader =
    DataLoader<List<AvatarFlycloakData>>("./ExcelBinOutput/AvatarFlycloakExcelConfigData.json")

val avatarFlycloakData get() = avatarFlycloakDataLoader.data

@Serializable
data class AvatarFlycloakData(
    @JsonNames("flycloakId", "FlycloakId")
    val flycloakId: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("prefabPath", "PrefabPath")
    val prefabPath: String,
    @JsonNames("jsonName", "JsonName")
    val jsonName: String,
    @JsonNames("icon", "Icon")
    val icon: String,
    @JsonNames("materialId", "MaterialId")
    val materialId: Int
)
