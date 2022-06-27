package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class AddProp(
    @JsonNames("propType", "PropType")
    val propType: FightProp = FightProp.FIGHT_PROP_NONE,
    @JsonNames("value", "Value")
    val value: Double = 0.0
)
