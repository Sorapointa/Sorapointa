package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.ItemParamData

private val envAnimalGatherDataLoader =
    DataLoader<List<EnvAnimalGatherData>>("./ExcelBinOutput/EnvAnimalGatherExcelConfigData.json")
val envAnimalGatherData get() = envAnimalGatherDataLoader.data

@Serializable
data class EnvAnimalGatherData(
    @JsonNames("aliveTime", "AliveTime")
    val aliveTime: Int? = null,
    @JsonNames("escapeTime", "EscapeTime")
    val escapeTime: Int? = null,
    @JsonNames("escapeRadius", "EscapeRadius")
    val escapeRadius: Int? = null,
    @JsonNames("animalId", "AnimalId")
    val animalId: Int,
    @JsonNames("entityType", "EntityType")
    val entityType: String,
    @JsonNames("gatherItemId", "GatherItemId")
    private val _gatherItemId: List<ItemParamData>,
    @JsonNames("excludeWeathers", "ExcludeWeathers")
    val excludeWeathers: String
) {
    val gatherItemId by lazy {
        _gatherItemId.filter { it.id != 0 }
    }
}
