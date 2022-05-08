package org.sorapointa.dataloader.custom

import kotlinx.serialization.SerialName
import org.sorapointa.dataloader.common.PointData

// TODO: 2022/5/8 ?
@kotlinx.serialization.Serializable
data class ScenePointEntry(
    @SerialName("name") val name: String,
    @SerialName("pointData") val pointData: PointData,
)
