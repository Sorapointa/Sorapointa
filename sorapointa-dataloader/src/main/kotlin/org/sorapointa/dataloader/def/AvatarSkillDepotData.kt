@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val avatarSkillDepotDataLoader =
    DataLoader<List<AvatarSkillDepotData>>("./ExcelBinOutput/AvatarSkillDepotExcelConfigData.json")

val avatarSkillDepotData get() = avatarSkillDepotDataLoader.data

val AvatarExcelData.skillDepotData
    get() = avatarSkillDepotData.firstOrNull { it.id == skillDepotId }

val AvatarExcelData.candSkillDepotData
    get() = avatarSkillDepotData.firstOrNull { it.id == skillDepotId }

@Serializable
data class AvatarSkillDepotData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("energySkill", "EnergySkill")
    private val _energySkill: Int? = null,
    @JsonNames("skills", "Skills")
    private val _skills: List<Int>,
    @JsonNames("subSkills", "SubSkills")
    val subSkills: List<Int>,
    @JsonNames("extraAbilities", "ExtraAbilities")
    val extraAbilities: List<String>,
    @JsonNames("talents", "Talents")
    val talents: List<Int>,
    @JsonNames("talentStarName", "TalentStarName")
    val talentStarName: String,
    @JsonNames("inherentProudSkillOpens", "InherentProudSkillOpens")
    private val _inherentProudSkillOpens: List<InherentProudSkillOpen>,
    @JsonNames("skillDepotAbilityGroup", "SkillDepotAbilityGroup")
    val skillDepotAbilityGroup: String,
    @JsonNames("leaderTalent", "LeaderTalent")
    val leaderTalent: Int? = null,
    @JsonNames("attackModeSkill", "AttackModeSkill")
    val attackModeSkill: Int? = null,
) {

    val skills by lazy {
        _skills.filter { it != 0 }
    }

    val inherentProudSkillOpens by lazy {
        _inherentProudSkillOpens.filter { it.proudSkillGroupId != 0 }
    }

    val energySkill by lazy {
        avatarSkillData.firstOrNull { _energySkill == it.id }
            ?: error("Could not find the avatarDepotId: $id, energy skill")
    }

    val normalAttack by lazy {
        avatarSkillData.firstOrNull { skills.getOrNull(0) == it.id }
            ?: error("Could not find the avatarDepotId: $id, normal attack skill")
    }

    val elementSkill by lazy {
        avatarSkillData.firstOrNull { skills.getOrNull(1) == it.id }
            ?: error("Could not find the avatarDepotId: $id, element skill")
    }

    @Serializable
    data class InherentProudSkillOpen(
        @JsonNames("proudSkillGroupId", "ProudSkillGroupId")
        val proudSkillGroupId: Int = 0,
        @JsonNames("needAvatarPromoteLevel", "NeedAvatarPromoteLevel")
        val needAvatarPromoteLevel: Int = 0,
    )
}
