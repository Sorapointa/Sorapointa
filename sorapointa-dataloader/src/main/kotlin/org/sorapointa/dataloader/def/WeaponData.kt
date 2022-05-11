package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeaponData(
    @SerialName("WeaponType") val weaponType: String,
    @SerialName("RankLevel") val rankLevel: Int,
    @SerialName("WeaponBaseExp") val weaponBaseExp: Int,
    @SerialName("SkillAffix") val skillAffix: List<Int>,
    @SerialName("WeaponProp") val weaponProp: List<WeaponProp>,
    @SerialName("AwakenTexture") val awakenTexture: String,
    @SerialName("AwakenLightMapTexture") val awakenLightMapTexture: String,
    @SerialName("AwakenIcon") val awakenIcon: String,
    @SerialName("WeaponPromoteId") val weaponPromoteId: Int,
    @SerialName("StoryId") val storyId: Int,
    @SerialName("AwakenCosts") val awakenCosts: List<Int>,
    @SerialName("GachaCardNameHashSuffix") val gachaCardNameHashSuffix: Long,
    @SerialName("ChargeEfficiency") val chargeEfficiency: Int,
    @SerialName("DestroyRule") val destroyRule: String,
    @SerialName("DestroyReturnMaterial") val destroyReturnMaterial: List<Int>,
    @SerialName("DestroyReturnMaterialCount") val destroyReturnMaterialCount: List<Int>,
    @SerialName("Id") val id: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("DescTextMapHash") val descTextMapHash: Long,
    @SerialName("Icon") val icon: String,
    @SerialName("ItemType") val itemType: String,
    @SerialName("Weight") val weight: Int,
    @SerialName("Rank") val rank: Int,
    @SerialName("GadgetId") val gadgetId: Int,
    @SerialName("AwakenMaterial") val awakenMaterial: Int,
    @SerialName("InitialLockState") val initialLockState: Int,
    @SerialName("UnRotate") val unRotate: Boolean
) {
    @Serializable
    data class WeaponProp(
        @SerialName("PropType") val propType: String,
        @SerialName("InitValue") val initValue: Double,
        @SerialName("Type") val type: String
    )
}
