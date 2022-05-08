package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarFlycloakData(
    @SerialName("FlycloakId") val flycloakId: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
)
