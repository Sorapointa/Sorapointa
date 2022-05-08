package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class WorldLevelDataItem(
    @SerialName("Level") val level: Int,
    @SerialName("MonsterLevel") val monsterLevel: Int
)
