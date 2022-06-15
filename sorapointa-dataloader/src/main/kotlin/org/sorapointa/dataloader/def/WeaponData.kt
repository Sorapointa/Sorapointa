package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val weaponDataLoader = DataLoader<List<WeaponData>>("./ExcelBinOutput/WeaponExcelConfigData.json")
val weaponData
    get() = weaponDataLoader.data

@Serializable
data class WeaponData(
    @JsonNames("weaponType", "WeaponType")
    val weaponType: String,
    @JsonNames("rankLevel", "RankLevel")
    val rankLevel: Int,
    @JsonNames("weaponBaseExp", "WeaponBaseExp")
    val weaponBaseExp: Int,
    @JsonNames("skillAffix", "SkillAffix")
    val skillAffix: List<Int>,
    @JsonNames("weaponProp", "WeaponProp")
    val weaponProp: List<WeaponProp>,
    @JsonNames("awakenTexture", "AwakenTexture")
    val awakenTexture: String,
    @JsonNames("awakenLightMapTexture", "AwakenLightMapTexture")
    val awakenLightMapTexture: String,
    @JsonNames("awakenIcon", "AwakenIcon")
    val awakenIcon: String,
    @JsonNames("weaponPromoteId", "WeaponPromoteId")
    val weaponPromoteId: Int,
    @JsonNames("storyId", "StoryId")
    val storyId: Int? = null,
    @JsonNames("awakenCosts", "AwakenCosts")
    val awakenCosts: List<Int>,
    @JsonNames("gachaCardNameHashSuffix", "GachaCardNameHashSuffix")
    val gachaCardNameHashSuffix: Long,
    @JsonNames("destroyRule", "DestroyRule")
    val destroyRule: String? = null,
    @JsonNames("destroyReturnMaterial", "DestroyReturnMaterial")
    val destroyReturnMaterial: List<Int>,
    @JsonNames("destroyReturnMaterialCount", "DestroyReturnMaterialCount")
    val destroyReturnMaterialCount: List<Int>,
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: Long,
    @JsonNames("icon", "Icon")
    val icon: String,
    @JsonNames("itemType", "ItemType")
    val itemType: String,
    @JsonNames("weight", "Weight")
    val weight: Int,
    @JsonNames("rank", "Rank")
    val rank: Int,
    @JsonNames("gadgetId", "GadgetId")
    val gadgetId: Int,
    @JsonNames("awakenMaterial", "AwakenMaterial")
    val awakenMaterial: Int? = null,
    @JsonNames("initialLockState", "InitialLockState")
    val initialLockState: Int? = null,
    @JsonNames("unRotate", "UnRotate")
    val unRotate: Boolean? = null
) {
    @Serializable
    data class WeaponProp(
        @JsonNames("propType", "PropType")
        val propType: String? = null,
        @JsonNames("initValue", "InitValue")
        val initValue: Double? = null,
        @JsonNames("type", "Type")
        val type: String
    )
}
