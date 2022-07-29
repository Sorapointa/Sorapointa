package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val codexMaterialDataLoader =
    DataLoader<List<CodexMaterialData>>("./ExcelBinOutput/MaterialCodexExcelConfigData.json")

val codexMaterialData get() = codexMaterialDataLoader.data

@Serializable
data class CodexMaterialData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("materialId", "MaterialId")
    val materialId: Int,
    @JsonNames("sortOrder", "SortOrder")
    val sortOrder: Int
)
