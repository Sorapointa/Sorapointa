package org.sorapointa.dataloader.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class OpenCondData(
    @JsonNames("condType", "CondType") val condType: String,
    @JsonNames("paramList", "ParamList") val paramList: List<Int>,
)
