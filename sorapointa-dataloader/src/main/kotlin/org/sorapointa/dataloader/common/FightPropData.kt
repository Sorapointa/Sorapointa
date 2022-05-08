package org.sorapointa.dataloader.common

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class FightPropData(
    @SerialName("PropType") val propType: String,
    val prop: FightProperty, // TODO: 2022/5/8 ?
    @SerialName("Value") val value: Float,
) {
    enum class FightProperty
}
