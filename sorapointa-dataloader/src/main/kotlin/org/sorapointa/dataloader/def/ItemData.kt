package org.sorapointa.dataloader.def

// TODO: 2022/5/8 Maybe has a bug.
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.common.FightPropData

@Serializable
data class ItemData(
    @SerialName("Id") val id: Int,
    @SerialName("StackLimit") val stackLimit: Int = 1,
    @SerialName("MaxUseCount") val maxUseCount: Int,
    @SerialName("RankLevel") val rankLevel: Int,
    @SerialName("EffectName") val effectName: String,
    @SerialName("StationParams") val stationParams: List<Int>, // Array
    @SerialName("Rank") val rank: Int,
    @SerialName("Weight") val weight: Int,
    @SerialName("GadgetId") val gadgetId: Int,
    @SerialName("DestroyReturnMaterial") val destroyReturnMaterial: List<Int>, // Array
    @SerialName("DestroyReturnMaterialCount") val destroyReturnMaterialCount: List<Int>, // Array
    // Food.
    @SerialName("FoodQuality") val foodQuality: String,
    @SerialName("UseTarget") val useTarget: String,
    @SerialName("UseParam") val useParam: List<String>, // Array
    // String enums.
    @SerialName("ItemType") val itemType: String,
    @SerialName("MaterialType") val materialType: String,
    @SerialName("EquipType") val equipType: String,
    @SerialName("EffectType") val effectType: String,
    @SerialName("DestroyRule") val destroyRule: String,
    // Relic.
    @SerialName("MainPropDepotId") val mainPropDepotId: Int,
    @SerialName("AppendPropDepotId") val appendPropDepotId: Int,
    @SerialName("SetId") val setId: Int,
    @SerialName("AddPropLevels") val addPropLevels: List<Int>, // Array
    @SerialName("BaseConvExp") val baseConvExp: Int,
    @SerialName("MaxLevel") val maxLevel: Int,
    // Weapon.
    @SerialName("WeaponPromoteId") val weaponPromoteId: Int,
    @SerialName("WeaponBaseExp") val weaponBaseExp: Int,
    @SerialName("StoryId") val storyId: Int,
    @SerialName("AvatarPromoteId") val avatarPromoteId: Int,
    @SerialName("AwakenCosts") val awakenCosts: List<Int>, // Array
    @SerialName("SkillAffix") val skillAffix: List<Int>, // Array
    @SerialName("WeaponProp") val weaponProp: List<WeaponProperty>, // Array
) {
    @Serializable
    data class WeaponProperty(
        @SerialName("FightProp") val fightProp: FightPropData.FightProperty,
        @SerialName("PropType") val propType: String,
        @SerialName("InitValue") val initValue: Float,
        @SerialName("Type") val type: String,
    )
}
