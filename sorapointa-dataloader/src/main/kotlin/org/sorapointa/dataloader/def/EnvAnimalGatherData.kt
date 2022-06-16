@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val envAnimalGatherDataLoader =
    DataLoader<List<EnvAnimalGatherData>>("./ExcelBinOutput/EnvAnimalGatherExcelConfigData.json")
val envAnimalGatherData get() = envAnimalGatherDataLoader.data

@Serializable
data class EnvAnimalGatherData(
    @JsonNames("animalId", "AnimalId")
    val animalId: Int,
    @JsonNames("entityType", "EntityType")
    val entityType: String,
    @JsonNames("gatherItemId", "GatherItemId")
    val gatherItemId: List<GatherItem>,
    @JsonNames("excludeWeathers", "ExcludeWeathers")
    val excludeWeathers: String,
    @JsonNames("aliveTime", "AliveTime")
    val aliveTime: Int,
    @JsonNames("escapeTime", "EscapeTime")
    val escapeTime: Int,
    @JsonNames("escapeRadius", "EscapeRadius")
    val escapeRadius: Int
) {
    @Serializable
    data class GatherItem(
        @JsonNames("id", "Id")
        val id: Int,
        @JsonNames("count", "Count")
        val count: Int
    )
}
