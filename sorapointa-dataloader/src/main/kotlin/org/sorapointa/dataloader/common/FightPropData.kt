package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class FightPropData(
    @JsonNames("propType", "PropType") val propType: String,
    val prop: FightProperty, // TODO: 2022/5/8 ?
    @JsonNames("value", "Value") val value: Float,
) {
    enum class FightProperty
}
