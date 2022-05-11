package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SceneData(
    @SerialName("Id") val id: Int,
    @SerialName("Type") val type: String,
    @SerialName("ScriptData") val scriptData: String,
    @SerialName("OverrideDefaultProfile") val overrideDefaultProfile: String,
    @SerialName("LevelEntityConfig") val levelEntityConfig: String,
    @SerialName("SpecifiedAvatarList") val specifiedAvatarList: List<Int>,
    @SerialName("Comment") val comment: String,
//    @SerialName("HPHMCOMLMPN") val hPHMCOMLMPN: Boolean,
    @SerialName("EntityAppearSorted") val entityAppearSorted: Int,
    @SerialName("MaxSpecifiedAvatarNum") val maxSpecifiedAvatarNum: Int,
//    @SerialName("IGEIMMEHIDG") val iGEIMMEHIDG: Boolean,
//    @SerialName("OGPODEBAOKA") val oGPODEBAOKA: Boolean
)
