package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.common.FetterCondType

private val fetterDataLoader =
    DataLoader<List<FetterData>>("./ExcelBinOutput/FettersExcelConfigData.json")

val fetterData get() = fetterDataLoader.data

@Serializable
data class FetterData(
    @JsonNames("isHiden", "IsHiden")
    val isHidden: Boolean = false,
    @JsonNames("tips", "Tips")
    val tips: List<Long>,
    @JsonNames("voiceTitleTextMapHash", "VoiceTitleTextMapHash")
    val voiceTitleTextMapHash: Long,
    @JsonNames("voiceFile", "VoiceFile")
    val voiceFile: String,
    @JsonNames("voiceFileTextTextMapHash", "VoiceFileTextTextMapHash")
    val voiceFileTextTextMapHash: Long,
    @JsonNames("voiceTitleLockedTextMapHash", "VoiceTitleLockedTextMapHash")
    val voiceTitleLockedTextMapHash: Long,
    @JsonNames("fetterId", "FetterId")
    val fetterId: Int,
    @JsonNames("avatarId", "AvatarId")
    val avatarId: Int,
    @JsonNames("openConds", "OpenConds")
    val openConds: List<OpenCond> = listOf(),
//    This one in json maybe has no data.
//    @JsonNames("finishConds", "FinishConds")
//    val finishConds: List<Any>
) {
    @Serializable
    data class OpenCond(
        @JsonNames("condType", "CondType")
        val condType: FetterCondType = FetterCondType.FETTER_COND_NONE,
        @JsonNames("paramList", "ParamList")
        val paramList: List<Int> = listOf()
    )
}
