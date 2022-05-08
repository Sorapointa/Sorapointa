package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarSkillDepotData(
    @SerialName("Id") val id: Int,
    @SerialName("EnergySkill") val energySkill: Int,
    @SerialName("AttackModeSkill") val attackModeSkill: Int,
    @SerialName("Skills") val skills: List<Int>,
    @SerialName("SubSkills") val subSkills: List<Int>,
    @SerialName("ExtraAbilities") val extraAbilities: List<String>,
    @SerialName("Talents") val talents: List<Int>,
    @SerialName("InherentProudSkillOpens") val inherentProudSkillOpens: List<InherentProudSkillOpens>,
    @SerialName("TalentStarName") val talentStarName: String,
    @SerialName("SkillDepotAbilityGroup") val skillDepotAbilityGroup: String,
    @SerialName("EnergySkillData") val energySkillData: AvatarSkillData,
    @SerialName("ElementType") val elementType: ElementType,
    @SerialName("Abilities") val abilities: List<Int>,
) {
    @Serializable
    data class InherentProudSkillOpens(
        @SerialName("ProudSkillGroupId") val proudSkillGroupId: Int,
        @SerialName("NeedAvatarPromoteLevel") val needAvatarPromoteLevel: Int,
    )

    enum class ElementType
}
