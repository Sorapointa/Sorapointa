@file:Suppress("unused")

package org.sorapointa.dispatch.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.sorapointa.utils.crypto.sha256sign
import org.sorapointa.utils.networkJson

@Serializable
data class QueryCurRegionData(
    val content: String,
    val sign: String = "sorapointa don't need to sign"
)

@Serializable
data class RegionListClientCustomConfig(
    @SerialName("sdkenv") val sdkEnvironment: UShort,
    @SerialName("showexception") val showException: Boolean,
    val loadPatch: Boolean,
    val regionConfig: String,
    val regionDispatchType: UShort,
    val videoKey: Long,
    val downloadMode: UShort
)

/**
 * Client Custom Config
 * @see [CodeSwitchData]
 * @see [CoverSwitchData]
 */
@Serializable
data class ClientCustomConfig(
    val codeSwitch: List<UShort>? = null,
    val coverSwitch: List<UShort>? = null,
    @SerialName("perf_report_enable") val perfReportEnable: Boolean? = null,
    @SerialName("perf_report_record_url") val perfReportRecordUrl: String? = null,
    @SerialName("perf_report_config_url") val perfReportConfigUrl: String? = null,
    val homeDotPattern: Boolean? = null,
    val homeItemFilter: UShort? = null,
    val reportNetDelayConfig: ReportNetDelayConfigData? = null
) {
    @Serializable
    data class ReportNetDelayConfigData(
        @SerialName("openGateserver") val openGateServer: Boolean? = null
    )

    @Serializable
    data class MtrConfigData(
        val isOpen: Boolean? = null
    )
}

@Serializable
class AgreementData(
    @SerialName("retcode") val returnCode: Short,
    val message: String,
    val data: Data? = null
) {
    @Serializable
    data class Data(
        @SerialName("marketing_agreements") val marketingAgreements: List<String>,
    )
}

@Serializable
class ComboConfigData(
    @SerialName("retcode") val returnCode: Short,
    val message: String,
    val data: Data? = null
) {
    @Serializable
    data class Data(
        val protocol: Boolean,
        @SerialName("qr_enabled") val qrEnabled: Boolean,
        @SerialName("log_level") val logLevel: String,
        @SerialName("announce_url") val announceUrl: String,
        @SerialName("push_alias_type") val pushAliasType: UShort,
        @SerialName("disable_ysdk_guard") val disableYsdkGuard: Boolean,
        @SerialName("enable_announce_pic_popup") val enableAnnouncePicPopup: Boolean,
    )
}

@Serializable
data class MdkShieldLoadConfigData(
    @SerialName("retcode") val returnCode: Short,
    val message: String,
    val data: Data? = null
) {
    @Serializable
    data class Data(
        val client: String,
        @SerialName("disable_mmt") val disableMmt: Boolean,
        @SerialName("disable_regist") val disableRegist: Boolean,
        @SerialName("enable_email_captcha") val enableEmailCaptcha: Boolean,
        @SerialName("enable_ps_bind_account") val enablePsBindAccount: Boolean,
        @SerialName("game_key") val gameKey: String,
        val guest: Boolean,
        val id: UShort,
        val identity: String,
        @SerialName("ignore_versions") val ignoreVersions: String,
        val name: String,
        val scene: String,
        @SerialName("server_guest") val serverGuest: Boolean,
        @SerialName("thirdparty") val thirdParty: List<String>,
        @SerialName("thirdparty_ignore") val thirdPartyIgnore: Map<String, String>,
        @SerialName("thirdparty_login_configs") val thirdPartyLoginConfigs: Map<String, ThirdPartyLoginConfigsData>
    ) {

        @Serializable
        data class ThirdPartyLoginConfigsData(
            @SerialName("token_type") val tokenType: String,
            @SerialName("game_token_expires_in") val gameTokenExpiresIn: UInt
        )
    }
}

@Serializable
data class PlatMVersionData(
    val version: UShort
)

@Serializable
data class ComboData(
    @SerialName("retcode") val returnCode: Short,
    val message: String,
    val data: Data? = null
) {
    @Serializable
    data class Data(
        @SerialName("vals") val values: Map<String, String>,
    )
}

@Serializable
data class CompareProtocolVersionData(
    @SerialName("retcode") val returnCode: Short,
    val message: String,
    val data: Data? = null
) {
    @Serializable
    data class Data(
        val modified: Boolean,
        val protocol: Protocol? = null
    ) {

        @Serializable
        data class Protocol(
            @SerialName("app_id") val appId: UShort,
            @SerialName("create_time") val createTime: UInt,
            val id: UShort,
            val language: String,
            val major: UShort,
            val minimum: UShort,
            @SerialName("user_proto") val userProto: String,
            @SerialName("priv_proto") val privProto: String,
            @SerialName("teenager_proto") val teenagerProto: String,
            @SerialName("third_proto") val thirdProto: String
        )
    }
}

@Serializable
data class ComboTokenRequestData(
    @SerialName("app_id") val appId: UShort,
    @SerialName("channel_id") val channelId: UShort,
    @SerialName("data") private val _data: String,
    val device: String,
    val sign: String
) {

    val data: LoginTokenData by lazy {
        networkJson.decodeFromString(_data)
    }

    @Serializable
    data class LoginTokenData(
        val uid: UInt,
        val guest: Boolean,
        val token: String,
    )

    fun signCheck(isChina: Boolean): Boolean {
        val calSign = "app_id=$appId&channel_id=$channelId&data=$_data&device=$device"
        val final = sha256sign(
            calSign,
            if (isChina) "d0d3a7342df2026a70f650b907800111" else "6a4c78fe0356ba4673b8071127b28123"
        )
        return final == sign
    }
}

@Serializable
data class ComboTokenResponseData(
    @SerialName("retcode") val returnCode: Short,
    val message: String,
    val data: LoginData? = null
) {

    @Serializable
    data class LoginData(
        @SerialName("account_type") val accountType: UShort,
        val heartbeat: Boolean = false,
        @SerialName("combo_id") val comboId: UInt,
        @SerialName("combo_token") val comboToken: String,
        @SerialName("open_id") val openId: UInt,
        val data: LoginGuestData = LoginGuestData(),
        @SerialName("fatigue_remind") val fatigueRemind: FatigueRemindData? = null,
    ) {

        @Serializable
        data class FatigueRemindData(
            val durations: List<UShort>,
            val nickname: String,
            @SerialName("reset_point") val resetPoint: UInt,
        )

        @Serializable
        data class LoginGuestData(val guest: Boolean = false)
    }
}

@Serializable
data class LoginAccountRequestData(
    val account: String,
    val password: String,
    @SerialName("is_crypto") val isCrypto: Boolean
)

@Serializable
data class LoginResultData(
    @SerialName("retcode") val returnCode: Short,
    val message: String,
    val data: VerifyData? = null
) {

    @Serializable
    data class VerifyData(
        val account: VerifyAccountData,
        @SerialName("device_grant_required") val deviceGrantRequired: Boolean = false,
        @SerialName("realname_operation") val realNameOperation: String = "None",
        @SerialName("realperson_required") val realPersonRequired: Boolean = false,
        @SerialName("safe_mobile_required") val safeMobileRequired: Boolean = false
    )

    @Serializable
    data class VerifyAccountData(
        val uid: UInt,
        val token: String,
        val name: String ? = null,
        val email: String ? = null,
        val mobile: String ? = null,
        val country: String ? = null,
        @SerialName("is_email_verify") val isEmailVerify: UShort ? = null,
        @SerialName("realname") val realName: String ? = null,
        @SerialName("identity_card") val identityCard: String ? = null,
        @SerialName("safe_mobile") val safeMobile: String? = null,
        @SerialName("facebook_name") val facebookName: String ? = null,
        @SerialName("twitter_name") val twitterName: String ? = null,
        @SerialName("game_center_name") val gameCenterName: String ? = null,
        @SerialName("google_name") val googleName: String ? = null,
        @SerialName("apple_name") val appleName: String ? = null,
        @SerialName("sony_name") val sonyName: String ? = null,
        @SerialName("tap_name") val tapName: String ? = null,
        @SerialName("reactivate_ticket") val reactivateTicket: String ? = null,
        @SerialName("area_code") val areaCode: String ? = null,
        @SerialName("device_grant_ticket") val deviceGrantTicket: String ? = null,
    )
}

@Serializable
data class VerifyTokenRequestData(
    val uid: UInt,
    val token: String
)
