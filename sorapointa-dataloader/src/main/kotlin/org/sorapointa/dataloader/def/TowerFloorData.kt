package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class TowerFloorData(
    @SerialName("FloorId") val floorId: Int,
    @SerialName("FloorIndex") val floorIndex: Int,
    @SerialName("LevelId") val levelId: Int,
    @SerialName("OverrideMonsterLevel") val overrideMonsterLevel: Int,
    @SerialName("TeamNum") val teamNum: Int,
    @SerialName("FloorLevelConfigId") val floorLevelConfigId: Int,
)
