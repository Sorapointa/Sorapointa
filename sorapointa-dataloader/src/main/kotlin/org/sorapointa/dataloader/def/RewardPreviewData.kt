package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val rewardPreviewLoader =
    DataLoader<List<RewardPreviewData>>("./ExcelBinOutput/RewardPreviewExcelConfigData.json")

val rewardPreviewData get() = rewardPreviewLoader.data

@Serializable
data class RewardPreviewData(
    @JsonNames("desc", "Desc")
    val desc: String,
    @JsonNames("previewItems", "PreviewItems")
    val previewItems: List<PreviewItem>,
    @JsonNames("id", "Id")
    val id: Int
) {
    @Serializable
    data class PreviewItem(
        @JsonNames("id", "Id")
        val id: Int,
        @JsonNames("count", "Count")
        val count: String
    )
}
