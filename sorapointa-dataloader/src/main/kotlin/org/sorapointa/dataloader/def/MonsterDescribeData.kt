package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MonsterDescribeData(
    @SerialName("Id") val id: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("TitleID") val titleID: Int,
    @SerialName("SpecialNameLabID") val specialNameLabID: Int,
    @SerialName("Icon") val icon: String
)
