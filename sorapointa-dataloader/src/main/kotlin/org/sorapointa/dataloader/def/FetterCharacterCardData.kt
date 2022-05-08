package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class FetterCharacterCardData(
    @SerialName("AvatarId") val avatarId: Int,
    @SerialName("FetterLevel") val fetterLevel: Int,
    @SerialName("RewardId") val rewardId: Int
)
