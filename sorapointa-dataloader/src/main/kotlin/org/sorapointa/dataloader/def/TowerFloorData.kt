package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TowerFloorData(
    @SerialName("FloorId") val floorId: Int,
    @SerialName("FloorIndex") val floorIndex: Int,
    @SerialName("LevelId") val levelId: Int,
    @SerialName("OverrideMonsterLevel") val overrideMonsterLevel: Int,
    @SerialName("TeamNum") val teamNum: Int,
    @SerialName("RewardIdFiveStars") val rewardIdFiveStars: Int,
    @SerialName("RewardIdTenStars") val rewardIdTenStars: Int,
    @SerialName("RewardIdFifteenStars") val rewardIdFifteenStars: Int,
    @SerialName("RewardIdThreeStars") val rewardIdThreeStars: Int,
    @SerialName("RewardIdSixStars") val rewardIdSixStars: Int,
    @SerialName("RewardIdNineStars") val rewardIdNineStars: Int,
    @SerialName("UnlockStarCount") val unlockStarCount: Int,
    @SerialName("FloorLevelConfigId") val floorLevelConfigId: Int,
    @SerialName("BgImage") val bgImage: String
)
