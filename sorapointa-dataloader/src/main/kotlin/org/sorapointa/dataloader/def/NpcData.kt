package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NpcData(
    @SerialName("JsonName") val jsonName: String,
    @SerialName("Alias") val alias: String,
    @SerialName("ScriptDataPath") val scriptDataPath: String,
    @SerialName("LuaDataPath") val luaDataPath: String,
    @SerialName("DyePart") val dyePart: String,
    @SerialName("BillboardIcon") val billboardIcon: String,
    @SerialName("TemplateEmotionPath") val templateEmotionPath: String,
    @SerialName("Id") val id: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("PrefabPathHashSuffix") val prefabPathHashSuffix: Long,
    @SerialName("PrefabPathHashPre") val prefabPathHashPre: Int,
    @SerialName("CampID") val campID: Int,
    @SerialName("LODPatternName") val lODPatternName: String
)
