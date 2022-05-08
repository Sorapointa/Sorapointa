package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GadgetData(
    @SerialName("CampID") val campID: Int,
    @SerialName("Id") val id: Int,
    @SerialName("InteeIconName") val inteeIconName: String,
    @SerialName("InteractNameTextMapHash") val interactNameTextMapHash: Int,
    @SerialName("ItemJsonName") val itemJsonName: String,
    @SerialName("JsonName") val jsonName: String,
    @SerialName("LODPatternName") val lODPatternName: String,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("PrefabPathHashPre") val prefabPathHashPre: Int,
    @SerialName("PrefabPathHashSuffix") val prefabPathHashSuffix: Int,
    @SerialName("Tags") val tags: List<String>,
    @SerialName("Type") val type: String
)
