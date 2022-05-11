package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarLevelData(
    @SerialName("Level") val level: Int,
    @SerialName("Exp") val exp: Int,
)
