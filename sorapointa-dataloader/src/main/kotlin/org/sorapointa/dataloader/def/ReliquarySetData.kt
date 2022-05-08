package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ReliquarySetData(
    @SerialName("SetId") val setId: Int,
    @SerialName("SetNeedNum") val setNeedNum: List<Int>, // Array
    @SerialName("EquipAffixId") val equipAffixId: Int,
    @SerialName("DisableFilter") val disableFilter: Int,
    @SerialName("ContainsList") val containsList: List<Int>, // Array
)
