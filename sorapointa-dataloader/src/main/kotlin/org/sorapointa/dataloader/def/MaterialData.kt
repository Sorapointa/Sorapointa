package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MaterialDataItem(
    @SerialName("InteractionTitleTextMapHash") val interactionTitleTextMapHash: Long,
    @SerialName("NoFirstGetHint") val noFirstGetHint: Boolean,
    @SerialName("UseParam") val useParam: List<String>,
    @SerialName("RankLevel") val rankLevel: Int,
    @SerialName("EffectDescTextMapHash") val effectDescTextMapHash: Long,
    @SerialName("SpecialDescTextMapHash") val specialDescTextMapHash: Long,
    @SerialName("TypeDescTextMapHash") val typeDescTextMapHash: Long,
    @SerialName("EffectIcon") val effectIcon: String,
    @SerialName("EffectName") val effectName: String,
    @SerialName("SatiationParams") val satiationParams: List<Int>,
//    Maybe has no data in json.
//    @SerialName("DestroyReturnMaterial") val destroyReturnMaterial: List<Any>,
//    @SerialName("DestroyReturnMaterialCount") val destroyReturnMaterialCount: List<Any>,
    @SerialName("Id") val id: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("DescTextMapHash") val descTextMapHash: Long,
    @SerialName("Icon") val icon: String,
    @SerialName("ItemType") val itemType: String,
    @SerialName("Rank") val rank: Int,
    @SerialName("EffectGadgetID") val effectGadgetID: Int,
    @SerialName("MaterialType") val materialType: String,
    @SerialName("GadgetId") val gadgetId: Int,
    @SerialName("PlayGainEffect") val playGainEffect: Boolean,
    @SerialName("StackLimit") val stackLimit: Int,
    @SerialName("MaxUseCount") val maxUseCount: Int,
    @SerialName("CloseBagAfterUsed") val closeBagAfterUsed: Boolean,
    @SerialName("UseTarget") val useTarget: String,
    @SerialName("UseLevel") val useLevel: Int,
    @SerialName("DestroyRule") val destroyRule: String,
    @SerialName("Weight") val weight: Int,
    @SerialName("IsForceGetHint") val isForceGetHint: Boolean,
    @SerialName("FoodQuality") val foodQuality: String,
    @SerialName("CdTime") val cdTime: Int,
    @SerialName("CdGroup") val cdGroup: Int,
    @SerialName("IsSplitDrop") val isSplitDrop: Boolean
)
