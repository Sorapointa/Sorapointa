package org.sorapointa.dataloader.common

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class OpenCondData(
    @SerialName("CondType") val condType: String,
    @SerialName("ParamList") val paramList: List<Int>,
)
