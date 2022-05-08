package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class WeaponLevelData(
    @SerialName("Level") val level: Int,
    @SerialName("RequiredExps") val requiredExps: List<Int>, // Array
)
