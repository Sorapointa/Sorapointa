package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.ElementType
import org.sorapointa.dataloader.common.acceptEnum

private val avatarSkillDataLoader =
    DataLoader<List<AvatarSkillData>>("./ExcelBinOutput/AvatarSkillExcelConfigData.json")

val avatarSkillData get() = avatarSkillDataLoader.data

@Serializable
data class AvatarSkillData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("abilityName", "AbilityName")
    val abilityName: String? = null,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("skillIcon", "SkillIcon")
    val skillIcon: String? = null,
    @JsonNames("costStamina", "CostStamina")
    val costStamina: Double? = null,
    @JsonNames("maxChargeNum", "MaxChargeNum")
    val maxChargeNum: Int,
    @JsonNames("lockShape", "LockShape")
    val lockShape: String,
    @JsonNames("lockWeightParams", "LockWeightParams")
    val lockWeightParams: List<Double>,
    @JsonNames("isAttackCameraLock", "IsAttackCameraLock")
    val isAttackCameraLock: Boolean = false,
    @JsonNames("buffIcon", "BuffIcon")
    val buffIcon: String? = null,
    @JsonNames("globalValueKey", "GlobalValueKey")
    val globalValueKey: String? = null,
    @JsonNames("cdTime", "CdTime")
    val cdTime: Double? = null,
    @JsonNames("triggerID", "TriggerID")
    val triggerID: Int? = null,
    @JsonNames("proudSkillGroupId", "ProudSkillGroupId")
    val proudSkillGroupId: Int? = null,
    @JsonNames("costElemType", "CostElemType")
    private val _costElemType: JsonPrimitive? = null,
    @JsonNames("costElemVal", "CostElemVal")
    val costElemVal: Double? = null,
    @JsonNames("ignoreCDMinusRatio", "IgnoreCDMinusRatio")
    val ignoreCDMinusRatio: Boolean? = null,
    @JsonNames("needStore", "NeedStore")
    val needStore: Boolean? = null,
    @JsonNames("defaultLocked", "DefaultLocked")
    val defaultLocked: Boolean? = null,
    @JsonNames("cdSlot", "CdSlot")
    val cdSlot: Int? = null,
    @JsonNames("energyMin", "EnergyMin")
    val energyMin: Double? = null
) {

    val costElemType by lazy {
        _costElemType?.let { acceptEnum(it, ElementType.None) }
    }
}
