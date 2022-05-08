package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import org.sorapointa.dataloader.common.FightPropData

@kotlinx.serialization.Serializable
data class ReliquaryMainPropData(
    @SerialName("Id") val id: Int,
    @SerialName("PropDepotId") val propDepotId: Int,
    @SerialName("PropType") val propType: String,
    @SerialName("AffixName") val affixName: String,
    @SerialName("Weight") val weight: Int,
    val fightProp: FightPropData.FightProperty
)
