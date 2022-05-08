package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarFetterLevelData(
    @SerialName("FetterLevel") val fetterLevel: Int,
    @SerialName("NeedExp") val needExp: Int,
)
