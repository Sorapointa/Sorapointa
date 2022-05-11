package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerLevelData(
    @SerialName("Level") val level: Int,
    @SerialName("Exp") val exp: Int,
    @SerialName("UnlockDescTextMapHash") val unlockDescTextMapHash: Long
)
