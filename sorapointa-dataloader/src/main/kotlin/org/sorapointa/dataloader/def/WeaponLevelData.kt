package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class WeaponLevelDataItem(
    @SerialName("Level") val level: Int,
    @SerialName("RequiredExps") val requiredExps: List<Int>
)

