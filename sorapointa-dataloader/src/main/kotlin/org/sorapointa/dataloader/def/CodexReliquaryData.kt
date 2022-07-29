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
    val cupId: Int? = null,
    @JsonNames("leatherId", "LeatherId")
    val leatherId: Int? = null,
    @JsonNames("capId", "CapId")
    val capId: Int,
    @JsonNames("flowerId", "FlowerId")
    val flowerId: Int? = null,
    @JsonNames("sandId", "SandId")
    val sandId: Int? = null,
    @JsonNames("sortOrder", "SortOrder")
    val sortOrder: Int
)
