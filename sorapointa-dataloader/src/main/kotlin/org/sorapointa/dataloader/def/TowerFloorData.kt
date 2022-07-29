package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val towerFloorLoader =
    DataLoader<List<TowerFloorData>>("./ExcelBinOutput/TowerFloorExcelConfigData.json")

val towerFloorData get() = towerFloorLoader.data

@Serializable
data class TowerFloorData(
    @JsonNames("floorId", "FloorId")
    val floorId: Int,
    @JsonNames("floorIndex", "FloorIndex")
    val floorIndex: Int,
    @JsonNames("levelGroupId", "LevelGroupId")
    val levelGroupId: Int,
    @JsonNames("overrideMonsterLevel", "OverrideMonsterLevel")
    val overrideMonsterLevel: Int,
    @JsonNames("teamNum", "TeamNum")
    val teamNum: Int,
    @JsonNames("rewardIdFiveStars", "RewardIdFiveStars")
    val rewardIdFiveStars: Int,
    @JsonNames("rewardIdTenStars", "RewardIdTenStars")
    val rewardIdTenStars: Int,
    @JsonNames("rewardIdFifteenStars", "RewardIdFifteenStars")
    val rewardIdFifteenStars: Int,
    @JsonNames("rewardIdThreeStars", "RewardIdThreeStars")
    val rewardIdThreeStars: Int,
    @JsonNames("rewardIdSixStars", "RewardIdSixStars")
    val rewardIdSixStars: Int,
    @JsonNames("rewardIdNineStars", "RewardIdNineStars")
    val rewardIdNineStars: Int,
    @JsonNames("unlockStarCount", "UnlockStarCount")
    val unlockStarCount: Int,
    @JsonNames("floorLevelConfigId", "FloorLevelConfigId")
    val floorLevelConfigId: Int,
    @JsonNames("bgImage", "BgImage")
    val bgImage: String
)
