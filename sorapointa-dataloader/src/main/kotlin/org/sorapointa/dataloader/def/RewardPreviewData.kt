package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import org.sorapointa.dataloader.common.ItemParamData
import org.sorapointa.dataloader.common.ItemParamStringData

data class RewardPreviewData(
    @SerialName("Id") val id: Int,
    @SerialName("PreviewItems") val previewItems: List<ItemParamStringData>,
    @SerialName("PreviewItemsArray") val previewItemsArray: List<ItemParamData>,
)
