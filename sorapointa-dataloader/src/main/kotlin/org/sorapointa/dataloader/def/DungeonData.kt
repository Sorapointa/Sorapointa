package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.DungeonStateType
import org.sorapointa.dataloader.common.ElementType

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
    val displayNameTextMapHash: Long,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("type", "Type")
    val type: String,
    @JsonNames("sceneId", "SceneId")
    val sceneId: Int,
    @JsonNames("involveType", "InvolveType")
    val involveType: String,
    @JsonNames("showLevel", "ShowLevel")
    val showLevel: Int? = null,
    @JsonNames("avatarLimitType", "AvatarLimitType")
    val avatarLimitType: Int? = null,
    @JsonNames("limitLevel", "LimitLevel")
    val limitLevel: Int? = null,
    @JsonNames("levelRevise", "LevelRevise")
    val levelRevise: Int? = null,
    @JsonNames("passCond", "PassCond")
    val passCond: Int? = null,
    @JsonNames("reviveMaxCount", "ReviveMaxCount")
    val reviveMaxCount: Int? = null,
    @JsonNames("dayEnterCount", "DayEnterCount")
    val dayEnterCount: Int? = null,
    @JsonNames("recommendElementTypes", "RecommendElementTypes")
    val recommendElementTypes: List<ElementType>,
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
    val stateType: DungeonStateType = DungeonStateType.DUNGEON_STATE_NONE
)
