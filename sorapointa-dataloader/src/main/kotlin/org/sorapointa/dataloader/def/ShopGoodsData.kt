package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShopGoodsData(
    @SerialName("GoodsId") val goodsId: Int,
    @SerialName("SubTagNameTextMapHash") val subTagNameTextMapHash: Long,
    @SerialName("ShopType") val shopType: Int,
    @SerialName("ItemId") val itemId: Int,
    @SerialName("ItemCount") val itemCount: Int,
    @SerialName("CostItems") val costItems: List<CostItem>,
    @SerialName("BeginTime") val beginTime: String,
    @SerialName("EndTime") val endTime: String,
//    Maybe has no data in json.
//    @SerialName("PreconditionParamList") val preconditionParamList: List<Any>,
    @SerialName("MinShowLevel") val minShowLevel: Int,
    @SerialName("MinPlayerLevel") val minPlayerLevel: Int,
    @SerialName("MaxPlayerLevel") val maxPlayerLevel: Int,
    @SerialName("SortLevel") val sortLevel: Int,
    @SerialName("BuyLimit") val buyLimit: Int,
    @SerialName("IsBuyOnce") val isBuyOnce: Boolean,
    @SerialName("Precondition") val precondition: String,
    @SerialName("CostScoin") val costScoin: Int,
    @SerialName("RefreshType") val refreshType: String,
    @SerialName("RefreshParam") val refreshParam: Int,
    @SerialName("SubTabId") val subTabId: Int,
    @SerialName("CostHcoin") val costHcoin: Int,
    @SerialName("RotateId") val rotateId: Int,
    @SerialName("DiscountRate") val discountRate: Double,
    @SerialName("OriginalPrice") val originalPrice: Int,
    @SerialName("PreconditionParam") val preconditionParam: Int,
    @SerialName("ShowId") val showId: Int,
    @SerialName("CostMcoin") val costMcoin: Int,
    @SerialName("ChooseOneGroupId") val chooseOneGroupId: Int
) {
    @Serializable
    data class CostItem(
        @SerialName("Id") val id: Int,
        @SerialName("Count") val count: Int
    )
}
