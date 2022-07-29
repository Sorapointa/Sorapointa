package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.ItemParamData

private val forgeDataLoader =
    DataLoader<List<ForgeData>>("./ExcelBinOutput/ForgeExcelConfigData.json")

val forgeData get() = forgeDataLoader.data

@Serializable
data class ForgeData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("playerLevel", "PlayerLevel")
    val playerLevel: Int,
    @JsonNames("isDefaultShow", "IsDefaultShow")
    val isDefaultShow: Boolean = false,
    @JsonNames("effectiveWorldLevels", "EffectiveWorldLevels")
    val effectiveWorldLevels: List<Int>,
    @JsonNames("forgeType", "ForgeType")
    val forgeType: Int,
    @JsonNames("showItemId", "ShowItemId")
    val showItemId: Int,
    @JsonNames("resultItemId", "ResultItemId")
    val resultItemId: Int? = null,
    @JsonNames("resultItemCount", "ResultItemCount")
    val resultItemCount: Int? = null,
    @JsonNames("forgeTime", "ForgeTime")
    val forgetTime: Int,
    @JsonNames("queueNum", "QueueNum")
    val queueNum: Int,
    @JsonNames("scoinCost", "ScoinCost")
    val scoinCost: Int,
    @JsonNames("randomItems", "RandomItems")
    private val _randomItems: List<ItemParamData>,
    @JsonNames("materialItems", "MaterialItems")
    private val _materialItems: List<ItemParamData>,
    @JsonNames("priority", "Priority")
    val priority: Int,
    @JsonNames("forgePoint", "ForgePoint")
    val forgePoint: Int? = null,
    @JsonNames("forgePointNoticeTextMapHash", "ForgePointNoticeTextMapHash")
    val forgePointNoticeTextMapHash: Long
) {

    val randomItems by lazy {
        _randomItems.filter { it.id != 0 }
    }

    val materialItems by lazy {
        _materialItems.filter { it.id != 0 }
    }
}
