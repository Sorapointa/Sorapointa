package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName

data class SceneData(
    @SerialName("Id") val id: Int,
    @SerialName("Type") val type: SceneType,
    @SerialName("ScriptData") val scriptData: String,
) {
    enum class SceneType
}
