package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import org.sorapointa.dataloader.common.ItemParamData

data class ShopGoodsData(
    @SerialName("GoodsId") val goodsId: Int,
    @SerialName("ShopType") val shopType: Int,
    @SerialName("ItemId") val itemId: Int,
    @SerialName("ItemCount") val itemCount: Int,
    @SerialName("CostScoin") val costScoin: Int,
    @SerialName("CostHcoin") val costHcoin: Int,
    @SerialName("CostMcoin") val costMcoin: Int,
    @SerialName("CostItems") val costItems: List<ItemParamData>,
    @SerialName("MinPlayerLevel") val minPlayerLevel: Int,
    @SerialName("MaxPlayerLevel") val maxPlayerLevel: Int,
    @SerialName("BuyLimit") val buyLimit: Int,
    @SerialName("SubTabId") val subTabId: Int,
    @SerialName("RefreshType") val refreshType: String,
    @SerialName("RefreshTypeEnum") val refreshTypeEnum: ShopRefreshType,
    @SerialName("RefreshParam") val refreshParam: Int,
) {
    enum class ShopRefreshType
}
