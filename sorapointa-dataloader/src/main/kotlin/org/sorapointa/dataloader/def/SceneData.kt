package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val sceneLoader =
    DataLoader<List<SceneData>>("./ExcelBinOutput/SceneExcelConfigData.json")

val sceneData get() = sceneLoader.data

fun findSceneData(id: Int) =
    sceneData.firstOrNull { it.id == id }

@Serializable
data class SceneData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("type", "Type")
    val type: String,
    @JsonNames("scriptData", "ScriptData")
    val scriptData: String,
    @JsonNames("overrideDefaultProfile", "OverrideDefaultProfile")
    val overrideDefaultProfile: String,
    @JsonNames("levelEntityConfig", "LevelEntityConfig")
    val levelEntityConfig: String,
    @JsonNames("specifiedAvatarList", "SpecifiedAvatarList")
    val specifiedAvatarList: List<Int>,
    @JsonNames("comment", "Comment")
    val comment: String,
    @JsonNames("entityAppearSorted", "EntityAppearSorted")
    val entityAppearSorted: Int? = null,
    @JsonNames("maxSpecifiedAvatarNum", "MaxSpecifiedAvatarNum")
    val maxSpecifiedAvatarNum: Int? = null,
)
