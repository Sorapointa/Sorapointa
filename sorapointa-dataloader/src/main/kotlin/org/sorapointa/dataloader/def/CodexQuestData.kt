package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val codexQuestDataLoader =
    DataLoader<List<CodexQuestData>>("./ExcelBinOutput/QuestCodexExcelConfigData.json")

val codexQuestData get() = codexQuestDataLoader.data

@Serializable
data class CodexQuestData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("parentQuestId", "ParentQuestId")
    val parentQuestId: Int,
    @JsonNames("chapterId", "ChapterId")
    val chapterId: Int,
    @JsonNames("sortOrder", "SortOrder")
    val sortOrder: Int,
    @JsonNames("isDisuse", "IsDisuse")
    val IsDisuse: Boolean
)
