@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val dailyDungeonDataLoader =
    DataLoader<List<DailyDungeonData>>("./ExcelBinOutput/DailyDungeonConfigData.json")

val dailyDungeonData get() = dailyDungeonDataLoader.data

@Serializable
data class DailyDungeonData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("monday", "Monday")
    val monday: List<Int>,
    @JsonNames("tuesday", "Tuesday")
    val tuesday: List<Int>,
    @JsonNames("wednesday", "Wednesday")
    val wednesday: List<Int>,
    @JsonNames("thursday", "Thursday")
    val thursday: List<Int>,
    @JsonNames("friday", "Friday")
    val friday: List<Int>,
    @JsonNames("saturday", "Saturday")
    val saturday: List<Int>,
    @JsonNames("sunday", "Sunday")
    val sunday: List<Int>
)
