package org.sorapointa.dataloader.custom

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.common.PointData

// TODO: 2022/5/8 ?
@Serializable
data class ScenePointEntry(
    @JsonNames("name", "Name") val name: String,
    @JsonNames("pointData", "PointData") val pointData: PointData,
)
