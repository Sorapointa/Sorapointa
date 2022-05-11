package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DungeonData(
    @SerialName("Id") val id: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("DisplayNameTextMapHash") val displayNameTextMapHash: Int,
    @SerialName("DescTextMapHash") val descTextMapHash: Long,
    @SerialName("Type") val type: String,
    @SerialName("SceneId") val sceneId: Int,
    @SerialName("InvolveType") val involveType: String,
    @SerialName("ShowLevel") val showLevel: Int,
    @SerialName("AvatarLimitType") val avatarLimitType: Int,
    @SerialName("LimitLevel") val limitLevel: Int,
    @SerialName("LevelRevise") val levelRevise: Int,
    @SerialName("PassCond") val passCond: Int,
    @SerialName("ReviveMaxCount") val reviveMaxCount: Int,
    @SerialName("DayEnterCount") val dayEnterCount: Int,
    @SerialName("RecommendElementTypes") val recommendElementTypes: List<Int>,
    @SerialName("SettleCountdownTime") val settleCountdownTime: Long,
    @SerialName("FailSettleCountdownTime") val failSettleCountdownTime: Int,
    @SerialName("QuitSettleCountdownTime") val quitSettleCountdownTime: Int,
    @SerialName("SettleShows") val settleShows: List<String>,
    @SerialName("LevelConfigMap") val levelConfigMap: Map<Int, Int>,
    @SerialName("CityID") val cityID: Int,
    @SerialName("EntryPicPath") val entryPicPath: String,
    @SerialName("StateType") val stateType: String
)
