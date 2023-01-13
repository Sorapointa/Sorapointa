package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val avatarSkillDepotDataLoader =
    DataLoader<List<AvatarSkillDepotData>>("./ExcelBinOutput/AvatarSkillDepotExcelConfigData.json")

val avatarSkillDepotData get() = avatarSkillDepotDataLoader.data

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
    val subSkills: List<Int>? = null,
    @JsonNames("extraAbilities", "ExtraAbilities")
    val extraAbilities: List<String>,
    @JsonNames("talents", "Talents")
    val talents: List<Int>,
    @JsonNames("talentStarName", "TalentStarName")
    val talentStarName: String? = null,
    @JsonNames("inherentProudSkillOpens", "InherentProudSkillOpens")
    private val _inherentProudSkillOpens: List<InherentProudSkillOpen>,
    @JsonNames("skillDepotAbilityGroup", "SkillDepotAbilityGroup")
    val skillDepotAbilityGroup: String? = null,
    @JsonNames("leaderTalent", "LeaderTalent")
    val leaderTalent: Int? = null,
    @JsonNames("attackModeSkill", "AttackModeSkill")
    val attackModeSkill: Int? = null,
) {

    val skills by lazy {
        _skills.filter { it != 0 }
    }

    val energySkillId = _energySkill
    val normalAttackSkillId = skills.getOrNull(0)
    val elementSkillId = skills.getOrNull(1)

    val inherentProudSkillOpens by lazy {
        _inherentProudSkillOpens.filter { it.proudSkillGroupId != 0 }
    }

    val energySkill by lazy {
        avatarSkillData.firstOrNull { _energySkill == it.id }
    }

    val normalAttack by lazy {
        avatarSkillData.firstOrNull { normalAttackSkillId == it.id }
    }

    val elementSkill by lazy {
        avatarSkillData.firstOrNull { elementSkillId == it.id }
    }

    @Serializable
    data class InherentProudSkillOpen(
        @JsonNames("proudSkillGroupId", "ProudSkillGroupId")
        val proudSkillGroupId: Int = 0,
        @JsonNames("needAvatarPromoteLevel", "NeedAvatarPromoteLevel")
        val needAvatarPromoteLevel: Int = 0,
    )
}
