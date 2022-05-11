package org.sorapointa.dispatch.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class RegionListClientCustomConfig(
    @SerialName("sdkenv") val sdkEnvironment: Int,
    @SerialName("showexception") val showException: Boolean,
    val loadPatch: Boolean,
    val regionConfig: String,
    val regionDispatchType: Int,
    val videoKey: Long,
    val downloadMode: Int
)

/**
 * Client Custom Config
 * @see [CodeSwitchData]
 * @see [CoverSwitchData]
 */
@Serializable
data class ClientCustomConfig(
    val codeSwitch: List<Int>? = null,
    val coverSwitch: List<Int>? = null,
    @SerialName("perf_report_enable") val perfReportEnable: Boolean? = null,
    @SerialName("perf_report_record_url") val perfReportRecordUrl: String? = null,
    @SerialName("perf_report_config_url") val perfReportConfigUrl: String? = null,
    val homeDotPattern: Boolean? = null,
    val homeItemFilter: Int? = null,
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
    @SerialName("retcode") val returnCode: Int,
    val message: String,
    val data: Data? = null
) {
    @Serializable
    data class Data(
        @SerialName("marketing_agreements") val marketingAgreements: ArrayList<String>,
    )
}

@Serializable
class ComboConfigData(
    @SerialName("retcode") val returnCode: Int,
    val message: String,
    val data: Data? = null
) {
    @Serializable
    data class Data(
        val protocol: Boolean,
        @SerialName("qr_enabled") val qrEnabled: Boolean,
        @SerialName("log_level") val logLevel: String,
        @SerialName("announce_url") val announceUrl: String,
        @SerialName("push_alias_type") val pushAliasType: Int,
        @SerialName("disable_ysdk_guard") val disableYsdkGuard: Boolean,
        @SerialName("enable_announce_pic_popup") val enableAnnouncePicPopup: Boolean,
    )
}

@Serializable
data class MdkShieldLoadConfigData(
    @SerialName("retcode") val returnCode: Int,
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
        val id: Int,
        val identity: String,
        @SerialName("ignore_versions") val ignoreVersions: String,
        val name: String,
        val scene: String,
        @SerialName("server_guest") val serverGuest: Boolean,
        @SerialName("thirdparty") val thirdParty: ArrayList<String>,
        @SerialName("thirdparty_ignore") val thirdPartyIgnore: Map<String, String>,
        @SerialName("thirdparty_login_configs") val thirdPartyLoginConfigs: Map<String, ThirdPartyLoginConfigsData>
    ) {

        @Serializable
        data class ThirdPartyLoginConfigsData(
            @SerialName("token_type") val tokenType: String,
            @SerialName("game_token_expires_in") val gameTokenExpiresIn: Int
        )

    }
}

@Serializable
data class PlatMVersionData(
    val version: Int
)

@Serializable
data class ComboData(
    @SerialName("retcode") val returnCode: Int,
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
    @SerialName("retcode") val returnCode: Int,
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
            @SerialName("app_id") val appId: Int,
            @SerialName("create_time") val createTime: Int,
            val id: Int,
            val language: String,
            val major: Int,
            val minimum: Int,
            @SerialName("user_proto") val userProto: String,
            @SerialName("priv_proto") val privProto: String,
            @SerialName("teenager_proto") val teenagerProto: String,
            @SerialName("third_proto") val thirdProto: String
        )

    }
}

@Serializable
data class ComboTokenRequestData(
    @SerialName("app_id") val appId: Int,
    @SerialName("channel_id") val channelId: Int,
    val data: String,
    val device: String,
    val sign: String
) {

    @Serializable
    data class LoginTokenData(
        val uid: String,
        val token: String,
        val guest: Boolean,
    )
}

@Serializable
data class ComboTokenResponseData(
    val message: String,
    @SerialName("retcode") val returnCode: Int,
    val data: LoginData
) {

    @Serializable
    data class LoginData(
        @SerialName("account_type") val accountType: Int,
        val heartbeat: Boolean = false,
        @SerialName("combo_id") val comboId: String,
        @SerialName("combo_token") val comboToken: String,
        @SerialName("open_id") val openId: String,
        val data: LoginGuestData = LoginGuestData(),
        @SerialName("fatigue_remind") val fatigueRemind: String,
    ) {

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
    val message: String,
    @SerialName("retcode") val returnCode: Int,
    val data: VerifyData
) {

    @Serializable
    data class VerifyData(
        val account: VerifyAccountData,
        @SerialName("device_grant_required") val deviceGrantRequired: Boolean,
        @SerialName("realname_operation") val realNameOperation: String,
        @SerialName("realperson_required") val realpersonRequired: Boolean,
        @SerialName("safe_mobile_required") val safeMobileRequired: Boolean
    )

    @Serializable
    data class VerifyAccountData(
        val uid: String,
        val name: String,
        val email: String,
        val mobile: String,
        @SerialName("is_email_verify") val isEmailVerify: String, // TODO: Why not just use boolean
        @SerialName("realname") val realName: String,
        @SerialName("identity_card") val identityCard: String,
        val token: String,
        @SerialName("safe_mobile") val safeMobile: String,
        @SerialName("facebook_name") val facebookName: String,
        @SerialName("twitter_name") val twitterName: String,
        @SerialName("game_center_name") val gameCenterName: String,
        @SerialName("google_name") val googleName: String,
        @SerialName("apple_name") val appleName: String,
        @SerialName("sony_name") val sonyName: String,
        @SerialName("tap_name") val tapName: String,
        val country: String,
        @SerialName("reactivate_ticket") val reactivateTicket: String,
        @SerialName("area_code") val areaCode: String,
        @SerialName("device_grant_ticket") val deviceGrantTicket: String,
    )
}

@Serializable
data class LoginTokenRequestData(
    val uid: String,
    val token: String
)
