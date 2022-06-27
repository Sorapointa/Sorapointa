@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val avatarTalentDataLoader =
    DataLoader<List<AvatarTalentData>>("./ExcelBinOutput/AvatarTalentExcelConfigData.json")

val avatarTalentData get() = avatarTalentDataLoader.data

@Serializable
data class AvatarTalentData(
    @JsonNames("talentId", "TalentId")
    val talentId: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("icon", "Icon")
    val icon: String,
    @JsonNames("prevTalent", "PrevTalent")
    val prevTalent: Int? = null,
    @JsonNames("mainCostItemId", "MainCostItemId")
    val mainCostItemId: Int,
    @JsonNames("mainCostItemCount", "MainCostItemCount")
    val mainCostItemCount: Int,
    @JsonNames("openConfig", "OpenConfig")
    val openConfig: String,
    @JsonNames("paramList", "ParamList")
    val paramList: List<Double>
)
