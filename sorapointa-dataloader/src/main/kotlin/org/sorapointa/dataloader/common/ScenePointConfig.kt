package org.sorapointa.dataloader.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonObject

@kotlinx.serialization.Serializable
data class ScenePointConfig(
    @SerialName("Points") val points: JsonObject // com.google.gson.JsonObject
)
