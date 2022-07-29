package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val npcLoader =
    DataLoader<List<NpcData>>("./ExcelBinOutput/NpcExcelConfigData.json")

val npcData get() = npcLoader.data

@Serializable
data class NpcData(
    @JsonNames("jsonName", "JsonName")
    val jsonName: String,
    @JsonNames("alias", "Alias")
    val alias: String,
    @JsonNames("scriptDataPath", "ScriptDataPath")
    val scriptDataPath: String,
    @JsonNames("luaDataPath", "LuaDataPath")
    val luaDataPath: String,
    @JsonNames("dyePart", "DyePart")
    val dyePart: String,
    @JsonNames("billboardIcon", "BillboardIcon")
    val billboardIcon: String,
    @JsonNames("templateEmotionPath", "TemplateEmotionPath")
    val templateEmotionPath: String,
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("nameTextMapHash", "NameTextMapHash")
    val nameTextMapHash: Long,
    @JsonNames("prefabPathHashSuffix", "PrefabPathHashSuffix")
    val prefabPathHashSuffix: Long,
    @JsonNames("prefabPathHashPre", "PrefabPathHashPre")
    val prefabPathHashPre: Int,
    @JsonNames("campID", "CampID")
    val campID: Int,
    @JsonNames("lODPatternName", "LODPatternName")
    val lODPatternName: String
)
