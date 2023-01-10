package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.ItemType
import org.sorapointa.dataloader.common.MaterialDestroyType
import org.sorapointa.dataloader.common.WeaponType
import org.sorapointa.dataloader.common.acceptEnum

private val weaponDataLoader = DataLoader<List<WeaponData>>("./ExcelBinOutput/WeaponExcelConfigData.json")
val weaponData
    get() = weaponDataLoader.data

@Serializable
data class WeaponData(
    @JsonNames("weaponType", "WeaponType")
    private val _weaponType: JsonPrimitive,
    @JsonNames("rankLevel", "RankLevel")
    override val rankLevel: Int,
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
    val awakenCosts: List<Int>? = null,
    @JsonNames("gachaCardNameHash", "GachaCardNameHash")
    val gachaCardNameHash: ULong,
    @JsonNames("destroyRule", "DestroyRule")
    private val _destroyRule: JsonPrimitive? = null,
    @JsonNames("destroyReturnMaterial", "DestroyReturnMaterial")
    val destroyReturnMaterial: List<Int>,
    @JsonNames("destroyReturnMaterialCount", "DestroyReturnMaterialCount")
    val destroyReturnMaterialCount: List<Int>,
    @JsonNames("id", "Id")
    override val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: ULong,
    @JsonNames("descTextMapHash", "DescTextMapHash")
    val descTextMapHash: ULong,
    @JsonNames("icon", "Icon")
    val icon: String,
    @JsonNames("itemType", "ItemType")
    private val _itemType: JsonPrimitive,
    @JsonNames("weight", "Weight")
    override val weight: Int,
    @JsonNames("rank", "Rank")
    override val rank: Int,
    @JsonNames("gadgetId", "GadgetId")
    override val gadgetId: Int,
    @JsonNames("awakenMaterial", "AwakenMaterial")
    val awakenMaterial: Int? = null,
    @JsonNames("initialLockState", "InitialLockState")
    val initialLockState: Int? = null,
    @JsonNames("unRotate", "UnRotate")
    val unRotate: Boolean? = null
) : ItemExcelData {

    val weaponType by lazy {
        acceptEnum(_weaponType, WeaponType.WEAPON_NONE)
    }

    override val itemType by lazy {
        acceptEnum(_itemType, ItemType.ITEM_NONE)
    }

    val destroyRule by lazy {
        _destroyRule?.let {
            acceptEnum(_destroyRule, MaterialDestroyType.DESTROY_NONE)
        }
    }

    @Serializable
    data class WeaponProp(
        @JsonNames("propType", "PropType")
        private val _propType: JsonPrimitive? = null,
        @JsonNames("initValue", "InitValue")
        val initValue: Double? = null,
        @JsonNames("type", "Type")
        val type: Int? = null,
    ) {
        val propType by lazy {
            _propType?.let {
                acceptEnum(_propType, WeaponType.WEAPON_NONE)
            }
        }
    }
}
