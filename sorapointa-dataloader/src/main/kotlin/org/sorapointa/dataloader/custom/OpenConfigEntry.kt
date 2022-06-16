package org.sorapointa.dataloader.custom

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

// TODO: 2022/5/8 ?
@Serializable
data class OpenConfigEntry(
    @JsonNames("name", "Name") val name: String,
    @JsonNames("addAbilities", "AddAbilities") val addAbilities: List<String>, // Array
    @JsonNames("extraTalentIndex", "ExtraTalentIndex") val extraTalentIndex: Int,
    @JsonNames("skillPointModifiers", "SkillPointModifiers") val skillPointModifiers: List<SkillPointModifier>
) {
    @Serializable
    data class SkillPointModifier(
        @JsonNames("skillId", "SkillId") val skillId: Int,
        @JsonNames("delta", "Delta") val delta: Int,
    )
}
