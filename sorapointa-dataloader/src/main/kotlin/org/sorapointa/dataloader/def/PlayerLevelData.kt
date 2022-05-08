package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class PlayerLevelData(
    @SerialName("Level") val level: Int,
    @SerialName("Exp") val exp: Int,
    @SerialName("RewardId") val rewardId: Int,
    @SerialName("UnlockWorldLevel") val unlockWorldLevel: Int,
)
