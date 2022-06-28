package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val avatarCostumeDataLoader =
    DataLoader<List<AvatarCostumeData>>("./ExcelBinOutput/AvatarCostumeExcelConfigData.json")

val avatarCostumeDataList get() = avatarCostumeDataLoader.data

@Serializable
data class AvatarCostumeData(
    @JsonNames("costumeId", "CostumeId")
    val costumeId: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("itemId", "ItemId")
    val itemId: Int? = null,
    @JsonNames("avatarId", "AvatarId")
    val avatarId: Int,
    @JsonNames("jsonName", "JsonName")
    val jsonName: String,
    @JsonNames("sideIconName", "SideIconName")
    val sideIconName: String,
    @JsonNames("hide", "Hide")
    val hide: Boolean = false,
    @JsonNames("isDefault", "IsDefault")
    val isDefault: Boolean = false
)
