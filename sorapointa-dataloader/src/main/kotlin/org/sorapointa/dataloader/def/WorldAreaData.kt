package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.ElementType

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
    val areaID2: Int? = null,
    @JsonNames("sceneId", "SceneID", "SceneId")
    val sceneId: Int,
    @JsonNames("elementType", "ElementType")
    val elementType: ElementType = ElementType.None
)
