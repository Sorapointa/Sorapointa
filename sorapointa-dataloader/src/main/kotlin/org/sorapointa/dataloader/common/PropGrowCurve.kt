package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class PropGrowCurve(
    @JsonNames("type", "Type") val type: FightProp = FightProp.FIGHT_PROP_NONE,
    @JsonNames("growCurve", "GrowCurve") val growCurve: GrowCurveType = GrowCurveType.GROW_CURVE_NONE,
)
