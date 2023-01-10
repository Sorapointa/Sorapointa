package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class PropGrowCurve(
    @JsonNames("type", "Type") private val _type: JsonPrimitive,
    @JsonNames("growCurve", "GrowCurve") private val _growCurve: JsonPrimitive,
) {

    val type by lazy {
        acceptEnum(_type, FightProp.FIGHT_PROP_NONE)
    }

    val growCurve by lazy {
        acceptEnum(_growCurve, GrowCurveType.GROW_CURVE_NONE)
    }
}
