@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.common.ItemParamData

// private val envAnimalGatherDataLoader =
//    DataLoader<List<EnvAnimalGatherData>>("./ExcelBinOutput/EnvAnimalGatherExcelConfigData.json")
// val envAnimalGatherData get() = envAnimalGatherDataLoader.data

@Serializable
sealed class EnvAnimalGatherData {

    abstract val animalId: Int
    abstract val entityType: String
    @Suppress("PropertyName")
    protected abstract val _gatherItemId: List<ItemParamData>
    abstract val excludeWeathers: String

    val gatherItemId by lazy {
        _gatherItemId.filter { it.id != 0 }
    }

    data class Gadget(
        @JsonNames("aliveTime", "AliveTime")
        val aliveTime: Int,
        @JsonNames("escapeTime", "EscapeTime")
        val escapeTime: Int,
        @JsonNames("escapeRadius", "EscapeRadius")
        val escapeRadius: Int,
        @JsonNames("animalId", "AnimalId")
        override val animalId: Int,
        @JsonNames("entityType", "EntityType")
        override val entityType: String,
        @JsonNames("gatherItemId", "GatherItemId")
        override val _gatherItemId: List<ItemParamData>,
        @JsonNames("excludeWeathers", "ExcludeWeathers")
        override val excludeWeathers: String
    ) : EnvAnimalGatherData()

    data class Monster(
        @JsonNames("animalId", "AnimalId")
        override val animalId: Int,
        @JsonNames("entityType", "EntityType")
        override val entityType: String,
        @JsonNames("gatherItemId", "GatherItemId")
        override val _gatherItemId: List<ItemParamData>,
        @JsonNames("excludeWeathers", "ExcludeWeathers")
        override val excludeWeathers: String
    ) : EnvAnimalGatherData()
}
