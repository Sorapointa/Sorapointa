package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarSkillDataItem(
    @SerialName("Id") val id: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("AbilityName") val abilityName: String,
    @SerialName("DescTextMapHash") val descTextMapHash: Long,
    @SerialName("SkillIcon") val skillIcon: String,
    @SerialName("CostStamina") val costStamina: Double,
    @SerialName("MaxChargeNum") val maxChargeNum: Int,
    @SerialName("LockShape") val lockShape: String,
    @SerialName("LockWeightParams") val lockWeightParams: List<Double>,
    @SerialName("IsAttackCameraLock") val isAttackCameraLock: Boolean,
    @SerialName("BuffIcon") val buffIcon: String,
    @SerialName("GlobalValueKey") val globalValueKey: String,
    @SerialName("CdTime") val cdTime: Double,
    @SerialName("TriggerID") val triggerID: Int,
    @SerialName("DragType") val dragType: String,
    @SerialName("ShowIconArrow") val showIconArrow: Boolean,
    @SerialName("ProudSkillGroupId") val proudSkillGroupId: Int,
    @SerialName("CostElemType") val costElemType: String,
    @SerialName("CostElemVal") val costElemVal: Double,
    @SerialName("IgnoreCDMinusRatio") val ignoreCDMinusRatio: Boolean,
    @SerialName("NeedStore") val needStore: Boolean,
    @SerialName("NeedMonitor") val needMonitor: String,
    @SerialName("DefaultLocked") val defaultLocked: Boolean,
    @SerialName("CdSlot") val cdSlot: Int,
    @SerialName("EnergyMin") val energyMin: Double
)
