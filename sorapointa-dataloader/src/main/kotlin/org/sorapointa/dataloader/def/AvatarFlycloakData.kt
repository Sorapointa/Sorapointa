package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarFlycloakData(
    @SerialName("FlycloakId") val flycloakId: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("DescTextMapHash") val descTextMapHash: Long,
    @SerialName("PrefabPath") val prefabPath: String,
    @SerialName("JsonName") val jsonName: String,
    @SerialName("Icon") val icon: String,
    @SerialName("MaterialId") val materialId: Int
)
