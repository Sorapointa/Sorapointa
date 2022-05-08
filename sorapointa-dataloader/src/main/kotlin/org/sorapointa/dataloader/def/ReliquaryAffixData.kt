package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import org.sorapointa.dataloader.common.FightPropData

@kotlinx.serialization.Serializable
data class ReliquaryAffixData(
    @SerialName("Id") val id: Int,
    @SerialName("DepotId") val depotId: Int,
    @SerialName("GroupId") val groupId: Int,
    @SerialName("PropType") val propType: String,
    @SerialName("PropValue") val propValue: Float,
    @SerialName("Weight") val weight: Int,
    @SerialName("UpgradeWeight") val upgradeWeight: Int,
    val fightProp: FightPropData.FightProperty,
)
