package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val towerScheduleDataLoader =
    DataLoader<List<TowerScheduleData>>("./ExcelBinOutput/TowerScheduleExcelConfigData.json")

val towerScheduleData get() = towerScheduleDataLoader.data

@Serializable
data class TowerScheduleData(
    @JsonNames("scheduleId", "ScheduleId")
    val scheduleId: Int,
    @JsonNames("entranceFloorId", "EntranceFloorId")
    val entranceFloorId: List<Int>,
    @JsonNames("monthlyLevelConfigId", "MonthlyLevelConfigId")
    val monthlyLevelConfigId: Int,
    @JsonNames("schedules", "Schedules")
    val schedules: List<ScheduleDetail>
) {
    @Serializable
    data class ScheduleDetail(
        @JsonNames("floorList", "FloorList")
        val floorList: List<Int>
    )
}
