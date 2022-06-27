package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.ItemParamData
import org.sorapointa.dataloader.common.RefreshType

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
    val itemId: Int = 0,
    @JsonNames("itemCount", "ItemCount")
    val itemCount: Int,
    @JsonNames("costItems", "CostItems")
    private val _costItems: List<ItemParamData>,
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
    val buyLimit: Int? = null,
    @JsonNames("isBuyOnce", "IsBuyOnce")
    val isBuyOnce: Boolean = false,
    @JsonNames("costScoin", "CostScoin")
    val costScoin: Int = 0,
    @JsonNames("refreshType", "RefreshType")
    val refreshType: RefreshType = RefreshType.SHOP_PRECONDITION_NONE,
    @JsonNames("refreshParam", "RefreshParam")
    val refreshParam: Int? = null,
    @JsonNames("subTabId", "SubTabId")
    val subTabId: Int? = null,
    @JsonNames("costHcoin", "CostHcoin")
    val costHcoin: Int = 0,
    @JsonNames("rotateId", "RotateId")
    val rotateId: Int? = null,
    @JsonNames("discountRate", "DiscountRate")
    val discountRate: Double? = null,
    @JsonNames("originalPrice", "OriginalPrice")
    val originalPrice: Int? = null,
    @JsonNames("showId", "ShowId")
    val showId: Int? = null,
    @JsonNames("costMcoin", "CostMcoin")
    val costMcoin: Int = 0,
    @JsonNames("chooseOneGroupId", "ChooseOneGroupId")
    val chooseOneGroupId: Int? = null
) {

    val costItem by lazy {
        _costItems.filter { it.id != 0 }
    }
}
