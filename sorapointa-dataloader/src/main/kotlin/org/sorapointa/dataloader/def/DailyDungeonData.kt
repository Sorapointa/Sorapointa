package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyDungeonData(
    @SerialName("Id") val id: Int,
    @SerialName("Monday") val monday: List<Int>,
    @SerialName("Tuesday") val tuesday: List<Int>,
    @SerialName("Wednesday") val wednesday: List<Int>,
    @SerialName("Thursday") val thursday: List<Int>,
    @SerialName("Friday") val friday: List<Int>,
    @SerialName("Saturday") val saturday: List<Int>,
    @SerialName("Sunday") val sunday: List<Int>
)
