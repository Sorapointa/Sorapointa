package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.common.OpenCondData

@Serializable
data class FetterData(
    @SerialName("AvatarId") val avatarId: Int,
    @SerialName("FetterId") val fetterId: Int,
    @SerialName("OpenCond") val openCond: List<OpenCondData>,
)
