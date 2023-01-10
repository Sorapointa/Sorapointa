package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class AddProp(
    @JsonNames("propType", "PropType")
    private val _propType: JsonPrimitive,
    @JsonNames("value", "Value")
    val value: Double = 0.0
) {

    val propType by lazy {
        acceptEnum(_propType, FightProp.FIGHT_PROP_NONE)
    }
}
