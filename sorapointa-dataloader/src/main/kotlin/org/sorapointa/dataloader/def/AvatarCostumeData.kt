package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarCostumeData(
    @SerialName("CostumeId") val costumeId: Int, // Not found in json?
    @SerialName("ItemId") val itemId: Int,
    @SerialName("AvatarId") val avatarId: Int, // Not found in json?
)
