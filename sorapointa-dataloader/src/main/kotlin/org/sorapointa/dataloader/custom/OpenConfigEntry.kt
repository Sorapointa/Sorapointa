package org.sorapointa.dataloader.custom

import kotlinx.serialization.SerialName

// TODO: 2022/5/8 ?
@kotlinx.serialization.Serializable
data class OpenConfigEntry(
    @SerialName("name") val name: String,
    @SerialName("addAbilities") val addAbilities: List<String>, // Array
    @SerialName("extraTalentIndex") val extraTalentIndex: Int,
    @SerialName("skillPointModifiers") val skillPointModifiers: List<SkillPointModifier>
) {
    @kotlinx.serialization.Serializable
    data class SkillPointModifier(
        @SerialName("skillId") val skillId: Int,
        @SerialName("delta") val delta: Int,
    )
}
