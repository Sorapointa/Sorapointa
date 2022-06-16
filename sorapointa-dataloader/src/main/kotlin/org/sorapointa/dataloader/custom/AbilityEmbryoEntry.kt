package org.sorapointa.dataloader.custom

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

// TODO: 2022/5/8 ?
@Serializable
data class AbilityEmbryoEntry(
    @JsonNames("name", "Name") val name: String,
    @JsonNames("abilities", "Abilities") val abilities: List<String>, // Array
)
