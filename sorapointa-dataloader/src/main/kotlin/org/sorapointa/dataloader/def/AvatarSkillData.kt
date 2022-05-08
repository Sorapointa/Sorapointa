package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarSkillData(
    @SerialName("Id") val id: Int,
    @SerialName("CdTime") val cdTime: Float,
    @SerialName("CostElemVal") val costElemVal: Int,
    @SerialName("MaxChargeNum") val maxChargeNum: Int,
    @SerialName("TriggerId") val triggerId: Int,
    @SerialName("IsAttackCameraLock") val isAttackCameraLock: Boolean,
    @SerialName("ProudSkillGroupId") val proudSkillGroupId: Int,
    @SerialName("CostElemType") val costElemType: String,
    @SerialName("LockWeightParams") val lockWeightParams: List<Float>,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("AbilityName") val abilityName: String,
    @SerialName("LockShape") val lockShape: String,
    @SerialName("GlobalValueKey") val globalValueKey: String,
)
