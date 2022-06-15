@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val avatarSkillDepotDataLoader =
    DataLoader<List<AvatarSkillDepotData>>("./ExcelBinOutput/AvatarSkillDepotExcelConfigData.json")

val avatarSkillDepotData get() = avatarSkillDepotDataLoader.data

@Serializable
data class AvatarSkillDepotData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("energySkill", "EnergySkill")
    val energySkill: Int,
    @JsonNames("skills", "Skills")
    val skills: List<Int>,
    @JsonNames("subSkills", "SubSkills")
    val subSkills: List<Int>,
    @JsonNames("extraAbilities", "ExtraAbilities")
    val extraAbilities: List<String>,
    @JsonNames("talents", "Talents")
    val talents: List<Int>,
    @JsonNames("talentStarName", "TalentStarName")
    val talentStarName: String,
    @JsonNames("inherentProudSkillOpens", "InherentProudSkillOpens")
    val inherentProudSkillOpens: List<InherentProudSkillOpen>,
    @JsonNames("skillDepotAbilityGroup", "SkillDepotAbilityGroup")
    val skillDepotAbilityGroup: String,
    @JsonNames("leaderTalent", "LeaderTalent")
    val leaderTalent: Int,
    @JsonNames("attackModeSkill", "AttackModeSkill")
    val attackModeSkill: Int
) {
    @Serializable
    data class InherentProudSkillOpen(
        @JsonNames("proudSkillGroupId", "ProudSkillGroupId")
        val proudSkillGroupId: Int,
        @JsonNames("needAvatarPromoteLevel", "NeedAvatarPromoteLevel")
        val needAvatarPromoteLevel: Int
    )
}
