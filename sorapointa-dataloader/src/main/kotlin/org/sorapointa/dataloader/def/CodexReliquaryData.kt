package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val codexReliquaryDataLoader =
    DataLoader<List<CodexReliquaryData>>("./ExcelBinOutput/ReliquaryCodexExcelConfigData.json")

val codexReliquaryData get() = codexReliquaryDataLoader.data

@Serializable
data class CodexReliquaryData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("suitId", "SuitId")
    val suitId: Int,
    @JsonNames("level", "Level")
    val level: Int,
    @JsonNames("cupId", "CupId")
    val cupId: Int,
    @JsonNames("leatherId", "LeatherId")
    val leatherId: Int,
    @JsonNames("capId", "CapId")
    val capId: Int,
    @JsonNames("flowerId", "FlowerId")
    val flowerId: Int,
    @JsonNames("sandId", "SandId")
    val sandId: Int,
    @JsonNames("sortOrder", "SortOrder")
    val sortOrder: Int
)
