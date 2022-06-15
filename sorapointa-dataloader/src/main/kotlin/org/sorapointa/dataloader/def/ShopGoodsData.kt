package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val shopGoodsLoader =
    DataLoader<List<ShopGoodsData>>("./ExcelBinOutput/ShopGoodsExcelConfigData.json")

val shopGoodsData get() = shopGoodsLoader.data

@Serializable
data class ShopGoodsData(
    @JsonNames("goodsId", "GoodsId")
    val goodsId: Int,
    @JsonNames("subTagNameTextMapHash", "SubTagNameTextMapHash")
    val subTagNameTextMapHash: Long,
    @JsonNames("shopType", "ShopType")
    val shopType: Int,
    @JsonNames("itemId", "ItemId")
    val itemId: Int,
    @JsonNames("itemCount", "ItemCount")
    val itemCount: Int,
    @JsonNames("costItems", "CostItems")
    val costItems: List<CostItem>,
    @JsonNames("beginTime", "BeginTime")
    val beginTime: String,
    @JsonNames("endTime", "EndTime")
    val endTime: String,
//    Maybe has no data in json.
//    @JsonNames("preconditionParamList", "PreconditionParamList")
//    val preconditionParamList: List<Any>,
    @JsonNames("minShowLevel", "MinShowLevel")
    val minShowLevel: Int,
    @JsonNames("minPlayerLevel", "MinPlayerLevel")
    val minPlayerLevel: Int,
    @JsonNames("maxPlayerLevel", "MaxPlayerLevel")
    val maxPlayerLevel: Int,
    @JsonNames("sortLevel", "SortLevel")
    val sortLevel: Int,
    @JsonNames("buyLimit", "BuyLimit")
    val buyLimit: Int,
    @JsonNames("isBuyOnce", "IsBuyOnce")
    val isBuyOnce: Boolean,
    @JsonNames("precondition", "Precondition")
    val precondition: String,
    @JsonNames("costScoin", "CostScoin")
    val costScoin: Int,
    @JsonNames("refreshType", "RefreshType")
    val refreshType: String,
    @JsonNames("refreshParam", "RefreshParam")
    val refreshParam: Int,
    @JsonNames("subTabId", "SubTabId")
    val subTabId: Int,
    @JsonNames("costHcoin", "CostHcoin")
    val costHcoin: Int,
    @JsonNames("rotateId", "RotateId")
    val rotateId: Int,
    @JsonNames("discountRate", "DiscountRate")
    val discountRate: Double,
    @JsonNames("originalPrice", "OriginalPrice")
    val originalPrice: Int,
    @JsonNames("preconditionParam", "PreconditionParam")
    val preconditionParam: Int,
    @JsonNames("showId", "ShowId")
    val showId: Int,
    @JsonNames("costMcoin", "CostMcoin")
    val costMcoin: Int,
    @JsonNames("chooseOneGroupId", "ChooseOneGroupId")
    val chooseOneGroupId: Int
) {
    @Serializable
    data class CostItem(
        @JsonNames("id", "Id")
        val id: Int,
        @JsonNames("count", "Count")
        val count: Int
    )
}
