package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReliquarySetData(
    @SerialName("SetId") val setId: Int,
    @SerialName("SetIcon") val setIcon: String,
    @SerialName("SetNeedNum") val setNeedNum: List<Int>,
    @SerialName("EquipAffixId") val equipAffixId: Int,
    @SerialName("ContainsList") val containsList: List<Int>,
    @SerialName("DisableFilter") val disableFilter: Int
)
