package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AvatarSkillDepotDataItem(
    @SerialName("Id") val id: Int,
    @SerialName("EnergySkill") val energySkill: Int,
    @SerialName("Skills") val skills: List<Int>,
    @SerialName("SubSkills") val subSkills: List<Int>,
    @SerialName("ExtraAbilities") val extraAbilities: List<String>,
    @SerialName("Talents") val talents: List<Int>,
    @SerialName("TalentStarName") val talentStarName: String,
    @SerialName("InherentProudSkillOpens") val inherentProudSkillOpens: List<InherentProudSkillOpen>,
    @SerialName("SkillDepotAbilityGroup") val skillDepotAbilityGroup: String,
    @SerialName("LeaderTalent") val leaderTalent: Int,
    @SerialName("AttackModeSkill") val attackModeSkill: Int
) {
    @Serializable
    data class InherentProudSkillOpen(
        @SerialName("ProudSkillGroupId") val proudSkillGroupId: Int,
        @SerialName("NeedAvatarPromoteLevel") val needAvatarPromoteLevel: Int
    )
}
