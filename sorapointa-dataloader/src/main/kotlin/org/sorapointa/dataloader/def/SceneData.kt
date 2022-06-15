package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val sceneLoader =
    DataLoader<List<SceneData>>("./ExcelBinOutput/SceneExcelConfigData.json")

val sceneData get() = sceneLoader.data

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
    val entityAppearSorted: Int,
    @JsonNames("maxSpecifiedAvatarNum", "MaxSpecifiedAvatarNum")
    val maxSpecifiedAvatarNum: Int,
)
