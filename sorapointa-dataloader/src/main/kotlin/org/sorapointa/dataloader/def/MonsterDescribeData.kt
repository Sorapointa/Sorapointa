package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val monsterDescribeLoader =
    DataLoader<List<MonsterDescribeData>>("./ExcelBinOutput/MonsterDescribeExcelConfigData.json")

val monsterDescribeData get() = monsterDescribeLoader.data

@Serializable
data class MonsterDescribeData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("titleID", "TitleID")
    val titleID: Int,
    @JsonNames("specialNameLabID", "SpecialNameLabID")
    val specialNameLabID: Int,
    @JsonNames("icon", "Icon")
    val icon: String
)
