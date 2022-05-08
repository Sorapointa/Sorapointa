package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DungeonData(
    @SerialName("Id") val id: Int,
    @SerialName("SceneId") val sceneId: Int,
    @SerialName("ShowLevel") val showLevel: Int,
    @SerialName("PassRewardPreviewID") val passRewardPreviewID: Int,
    @SerialName("InvolveType: String, // TODO") val involveType: String, // TODO: 2022/5/8 enum (by GC)
)
