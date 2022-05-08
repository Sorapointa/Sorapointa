package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName


@kotlinx.serialization.Serializable
data class NpcData(
    @SerialName("Id") val id: Int,
    @SerialName("JsonName") val jsonName: String,
    @SerialName("Alias") val alias: String,
    @SerialName("ScriptDataPath") val scriptDataPath: String,
    @SerialName("LuaDataPath") val luaDataPath: String,
    @SerialName("IsInteractive") val isInteractive: Boolean,
    @SerialName("HasMove") val hasMove: Boolean,
    @SerialName("DyePart") val dyePart: String,
    @SerialName("BillboardIcon") val billboardIcon: String,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("CampID") val campId: Int,
)
