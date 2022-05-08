package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.common.FightPropData

@Serializable
data class EquipAffixData(
    @SerialName("AffixId") val affixId: Int,
    @SerialName("Id") val id: Int,
    @SerialName("Level") val level: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("OpenConfig") val openConfig: String,
    @SerialName("AddProps") val addProps: List<FightPropData>, // Array
    @SerialName("ParamList") val paramList: List<Float>, // Array
)
