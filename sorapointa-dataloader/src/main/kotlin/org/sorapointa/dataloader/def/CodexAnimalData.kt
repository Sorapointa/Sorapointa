package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val codexAnimalDataLoader =
    DataLoader<List<CodexAnimalData>>("./ExcelBinOutput/AnimalCodexExcelConfigData.json")

val codexAnimalData get() = codexAnimalDataLoader.data

@Serializable
data class CodexAnimalData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("type", "Type")
    val type: String,
    @JsonNames("describeId", "DescribeId")
    val describeId: Int,
    @JsonNames("sortOrder", "SortOrder")
    val sortOrder: Int
)
