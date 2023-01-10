package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject

@Serializable
data class ScenePointConfig(
    @JsonNames("points", "Points") val points: JsonObject
)
