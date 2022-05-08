package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RewardPreviewDataItem(
    @SerialName("Desc") val desc: String,
    @SerialName("PreviewItems") val previewItems: List<PreviewItem>,
    @SerialName("Id") val id: Int
) {
    @Serializable
    data class PreviewItem(
        @SerialName("Id") val id: Int,
        @SerialName("Count") val count: String
    )
}

