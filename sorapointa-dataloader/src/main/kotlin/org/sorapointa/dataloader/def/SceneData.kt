package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.SceneType
import org.sorapointa.dataloader.common.acceptEnum

private val sceneLoader =
    DataLoader<List<SceneData>>("./ExcelBinOutput/SceneExcelConfigData.json")

val sceneData get() = sceneLoader.data

fun findSceneData(id: Int) =
    sceneData.firstOrNull { it.id == id }

@Serializable
data class SceneData(
    @JsonNames("id", "Id")
    val id: Int = -1,
    @JsonNames("type", "Type")
    private val _type: JsonPrimitive? = null,
    @JsonNames("scriptData", "ScriptData")
    val scriptData: String? = null,
    @JsonNames("overrideDefaultProfile", "OverrideDefaultProfile")
    val overrideDefaultProfile: String? = null,
    @JsonNames("levelEntityConfig", "LevelEntityConfig")
    val levelEntityConfig: String? = null,
    @JsonNames("specifiedAvatarList", "SpecifiedAvatarList")
    val specifiedAvatarList: List<Int>? = null,
    @JsonNames("comment", "Comment")
    val comment: String? = null,
    @JsonNames("entityAppearSorted", "EntityAppearSorted")
    val entityAppearSorted: Int? = null,
    @JsonNames("maxSpecifiedAvatarNum", "MaxSpecifiedAvatarNum")
    val maxSpecifiedAvatarNum: Int? = null,
) {

    val type by lazy {
        _type?.let { acceptEnum(it, SceneType.SCENE_NONE) }
    }
}
