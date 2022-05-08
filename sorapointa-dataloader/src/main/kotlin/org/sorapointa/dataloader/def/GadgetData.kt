package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GadgetData(
    @SerialName("Id") val id: Int,
    @SerialName("Type") val type: EntityType,
    @SerialName("JsonName") val jsonName: String,
    @SerialName("IsInteractive") val isInteractive: Boolean,
    @SerialName("Tags") val tags: List<String>, // Array
    @SerialName("ItemJsonName") val itemJsonName: String,
    @SerialName("InteeIconName") val inteeIconName: String,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("CampID") val campID: Int,
    @SerialName("LodPatternName") val lodPatternName: String,
) {
    enum class EntityType
}
