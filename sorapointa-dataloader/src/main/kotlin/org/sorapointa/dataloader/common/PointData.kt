package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

// TODO: 2022/5/8 ?
@Serializable
data class PointData(
    @JsonNames("id", "Id") val id: Int,
    @JsonNames("\$type", "\$Type") val type: String,
    @JsonNames("transPos", "TransPos") val transPos: Triple<Int, Int, Int>, // Position
    @JsonNames("dungeonIds", "DungeonIds") val dungeonIds: List<Int>, // Array
    @JsonNames("dungeonRandomList", "DungeonRandomList") val dungeonRandomList: List<Int> // Array
)
