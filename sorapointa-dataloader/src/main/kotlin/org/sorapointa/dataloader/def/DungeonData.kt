@file:OptIn(ExperimentalSerializationApi::class)

package org.sorapointa.dataloader.def

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val dungeonDataLoader =
    DataLoader<List<DungeonData>>("./ExcelBinOutput/DungeonExcelConfigData.json")

val dungeonData get() = dungeonDataLoader.data

@Serializable
data class DungeonData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("displayNameTextMapHash", "DisplayNameTextMapHash")
    val displayNameTextMapHash: Int,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("type", "Type")
    val type: String,
    @JsonNames("sceneId", "SceneId")
    val sceneId: Int,
    @JsonNames("involveType", "InvolveType")
    val involveType: String,
    @JsonNames("showLevel", "ShowLevel")
    val showLevel: Int,
    @JsonNames("avatarLimitType", "AvatarLimitType")
    val avatarLimitType: Int,
    @JsonNames("limitLevel", "LimitLevel")
    val limitLevel: Int,
    @JsonNames("levelRevise", "LevelRevise")
    val levelRevise: Int,
    @JsonNames("passCond", "PassCond")
    val passCond: Int,
    @JsonNames("reviveMaxCount", "ReviveMaxCount")
    val reviveMaxCount: Int,
    @JsonNames("dayEnterCount", "DayEnterCount")
    val dayEnterCount: Int,
    @JsonNames("recommendElementTypes", "RecommendElementTypes")
    val recommendElementTypes: List<Int>,
    @JsonNames("settleCountdownTime", "SettleCountdownTime")
    val settleCountdownTime: Long,
    @JsonNames("failSettleCountdownTime", "FailSettleCountdownTime")
    val failSettleCountdownTime: Int,
    @JsonNames("quitSettleCountdownTime", "QuitSettleCountdownTime")
    val quitSettleCountdownTime: Int,
    @JsonNames("settleShows", "SettleShows")
    val settleShows: List<String>,
    @JsonNames("levelConfigMap", "LevelConfigMap")
    val levelConfigMap: Map<Int, Int>,
    @JsonNames("cityID", "CityID")
    val cityID: Int,
    @JsonNames("entryPicPath", "EntryPicPath")
    val entryPicPath: String,
    @JsonNames("stateType", "StateType")
    val stateType: String
)
