package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class EquipAffixData(
    @SerialName("AffixId") val affixId: Int,
    @SerialName("Id") val id: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("DescTextMapHash") val descTextMapHash: Long,
    @SerialName("OpenConfig") val openConfig: String,
    @SerialName("AddProps") val addProps: List<AddProp>,
    @SerialName("ParamList") val paramList: List<Double>
) {
    @Serializable
    data class AddProp(
        @SerialName("PropType") val propType: String,
        @SerialName("Value") val value: Double
    )
}
