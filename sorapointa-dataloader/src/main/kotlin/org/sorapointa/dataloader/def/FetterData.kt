package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val fetterDataLoader =
    DataLoader<List<FetterData>>("./ExcelBinOutput/FettersExcelConfigData.json")

val fetterData get() = fetterDataLoader.data

@Serializable
data class FetterData(
    @JsonNames("isHiden", "IsHiden")
    val isHiden: Int,
    @JsonNames("tips", "Tips")
    val tips: List<Long>,
    @JsonNames("voiceTitleTextMapHash", "VoiceTitleTextMapHash")
    val voiceTitleTextMapHash: Int,
    @JsonNames("voiceFile", "VoiceFile")
    val voiceFile: String,
    @JsonNames("voiceFileTextTextMapHash", "VoiceFileTextTextMapHash")
    val voiceFileTextTextMapHash: Long,
    @JsonNames("voiceTitleLockedTextMapHash", "VoiceTitleLockedTextMapHash")
    val voiceTitleLockedTextMapHash: Int,
    @JsonNames("fetterId", "FetterId")
    val fetterId: Int,
    @JsonNames("avatarId", "AvatarId")
    val avatarId: Int,
    @JsonNames("openConds", "OpenConds")
    val openConds: List<OpenCond>,
//    This one in json maybe has no data.
//    @JsonNames("finishConds", "FinishConds")
//    val finishConds: List<Any>
) {
    @Serializable
    data class OpenCond(
        @JsonNames("condType", "CondType")
        val condType: String,
        @JsonNames("paramList", "ParamList")
        val paramList: List<Int>
    )
}
