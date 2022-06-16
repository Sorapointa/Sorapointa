package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val worldAreaDataLoader =
    DataLoader<List<WorldAreaData>>("./ExcelBinOutput/WorldAreaConfigData.json")

val worldAreaData get() = worldAreaDataLoader.data

@Serializable
data class WorldAreaData(
    @JsonNames("id", "Id", "ID")
    val id: Int,
    @JsonNames("areaID1", "AreaID1", "AreaId1")
    val areaID1: Int,
    @JsonNames("areaID2", "AreaID2", "AreaId2")
    val areaID2: Int,
    @JsonNames("sceneId", "SceneID", "SceneId")
    val sceneId: Int,
    @JsonNames("elementType", "ElementType")
    val elementType: String // TODO: To ElementType
)
