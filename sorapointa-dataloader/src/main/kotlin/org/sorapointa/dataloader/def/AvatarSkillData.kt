@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.ElementType

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
    val abilityName: String,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("skillIcon", "SkillIcon")
    val skillIcon: String,
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
    val buffIcon: String,
    @JsonNames("globalValueKey", "GlobalValueKey")
    val globalValueKey: String,
    @JsonNames("cdTime", "CdTime")
    val cdTime: Double = 0.0,
    @JsonNames("triggerID", "TriggerID")
    val triggerID: Int? = null,
    @JsonNames("proudSkillGroupId", "ProudSkillGroupId")
    val proudSkillGroupId: Int? = null,
    @JsonNames("costElemType", "CostElemType")
    val costElemType: ElementType = ElementType.None,
    @JsonNames("costElemVal", "CostElemVal")
    val costElemVal: Double = 0.0,
    @JsonNames("ignoreCDMinusRatio", "IgnoreCDMinusRatio")
    val ignoreCDMinusRatio: Boolean = false,
    @JsonNames("needStore", "NeedStore")
    val needStore: Boolean = false,
    @JsonNames("defaultLocked", "DefaultLocked")
    val defaultLocked: Boolean = false,
    @JsonNames("cdSlot", "CdSlot")
    val cdSlot: Int? = null,
    @JsonNames("energyMin", "EnergyMin")
    val energyMin: Double? = null
)
