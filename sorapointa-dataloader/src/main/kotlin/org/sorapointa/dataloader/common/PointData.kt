package org.sorapointa.dataloader.common

import kotlinx.serialization.SerialName

// TODO: 2022/5/8 ?
@kotlinx.serialization.Serializable
data class PointData(
    @SerialName("id") val id: Int,
    @SerialName("${'$'}type") val type: String,
    @SerialName("transPos") val transPos: Triple<Int, Int, Int>, // Position
    @SerialName("dungeonIds") val dungeonIds: List<Int>, // Array
    @SerialName("dungeonRandomList") val dungeonRandomList: List<Int> // Array
)
