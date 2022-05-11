package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FetterData(
    @SerialName("IsHiden") val isHiden: Int,
    @SerialName("Tips") val tips: List<Long>,
    @SerialName("VoiceTitleTextMapHash") val voiceTitleTextMapHash: Int,
    @SerialName("VoiceFile") val voiceFile: String,
    @SerialName("VoiceFileTextTextMapHash") val voiceFileTextTextMapHash: Long,
    @SerialName("VoiceTitleLockedTextMapHash") val voiceTitleLockedTextMapHash: Int,
    @SerialName("FetterId") val fetterId: Int,
    @SerialName("AvatarId") val avatarId: Int,
    @SerialName("OpenConds") val openConds: List<OpenCond>,
//    This one in json maybe has no data.
//    @SerialName("FinishConds") val finishConds: List<Any>
) {
    @Serializable
    data class OpenCond(
        @SerialName("CondType") val condType: String,
        @SerialName("ParamList") val paramList: List<Int>
    )
}
