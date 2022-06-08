package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val weaponDataLoader = DataLoader<List<WeaponData>>("./ExcelBinOutput/WeaponExcelConfigData.json")
val weaponData
    get() = weaponDataLoader.data

@Serializable
data class WeaponData(
    @SerialName("weaponType") val weaponType: String,
    @SerialName("rankLevel") val rankLevel: Int,
    @SerialName("weaponBaseExp") val weaponBaseExp: Int,
    @SerialName("skillAffix") val skillAffix: List<Int>,
    @SerialName("weaponProp") val weaponProp: List<WeaponProp>,
    @SerialName("awakenTexture") val awakenTexture: String,
    @SerialName("awakenLightMapTexture") val awakenLightMapTexture: String,
    @SerialName("awakenIcon") val awakenIcon: String,
    @SerialName("weaponPromoteId") val weaponPromoteId: Int,
    @SerialName("storyId") val storyId: Int? = null,
    @SerialName("awakenCosts") val awakenCosts: List<Int>,
    @SerialName("gachaCardNameHashSuffix") val gachaCardNameHashSuffix: Long,
    @SerialName("destroyRule") val destroyRule: String? = null,
    @SerialName("destroyReturnMaterial") val destroyReturnMaterial: List<Int>,
    @SerialName("destroyReturnMaterialCount") val destroyReturnMaterialCount: List<Int>,
    @SerialName("id") val id: Int,
    @SerialName("nameTextMapHash") val nameTextMapHash: Long,
    @SerialName("descTextMapHash") val descTextMapHash: Long,
    @SerialName("icon") val icon: String,
    @SerialName("itemType") val itemType: String,
    @SerialName("weight") val weight: Int,
    @SerialName("rank") val rank: Int,
    @SerialName("gadgetId") val gadgetId: Int,
    @SerialName("awakenMaterial") val awakenMaterial: Int? = null,
    @SerialName("initialLockState") val initialLockState: Int? = null,
    @SerialName("unRotate") val unRotate: Boolean? = null
) {
    @Serializable
    data class WeaponProp(
        @SerialName("propType") val propType: String? = null,
        @SerialName("initValue") val initValue: Double? = null,
        @SerialName("type") val type: String
    )
}
