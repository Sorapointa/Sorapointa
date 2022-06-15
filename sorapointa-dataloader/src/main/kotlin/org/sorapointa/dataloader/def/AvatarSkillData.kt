@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

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
    val costStamina: Double,
    @JsonNames("maxChargeNum", "MaxChargeNum")
    val maxChargeNum: Int,
    @JsonNames("lockShape", "LockShape")
    val lockShape: String,
    @JsonNames("lockWeightParams", "LockWeightParams")
    val lockWeightParams: List<Double>,
    @JsonNames("isAttackCameraLock", "IsAttackCameraLock")
    val isAttackCameraLock: Boolean,
    @JsonNames("buffIcon", "BuffIcon")
    val buffIcon: String,
    @JsonNames("globalValueKey", "GlobalValueKey")
    val globalValueKey: String,
    @JsonNames("cdTime", "CdTime")
    val cdTime: Double,
    @JsonNames("triggerID", "TriggerID")
    val triggerID: Int,
    @JsonNames("dragType", "DragType")
    val dragType: String,
    @JsonNames("showIconArrow", "ShowIconArrow")
    val showIconArrow: Boolean,
    @JsonNames("proudSkillGroupId", "ProudSkillGroupId")
    val proudSkillGroupId: Int,
    @JsonNames("costElemType", "CostElemType")
    val costElemType: String,
    @JsonNames("costElemVal", "CostElemVal")
    val costElemVal: Double,
    @JsonNames("ignoreCDMinusRatio", "IgnoreCDMinusRatio")
    val ignoreCDMinusRatio: Boolean,
    @JsonNames("needStore", "NeedStore")
    val needStore: Boolean,
    @JsonNames("needMonitor", "NeedMonitor")
    val needMonitor: String,
    @JsonNames("defaultLocked", "DefaultLocked")
    val defaultLocked: Boolean,
    @JsonNames("cdSlot", "CdSlot")
    val cdSlot: Int,
    @JsonNames("energyMin", "EnergyMin")
    val energyMin: Double
)
