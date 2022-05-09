package org.sorapointa.dispatch.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

//
// class RegionData(prq: QueryCurrRegionHttpRsp, b64: String) {
//    val parsedRegionQuery: QueryCurrRegionHttpRsp
//    val Base64: String
//
//    init {
//        parsedRegionQuery = prq
//        Base64 = b64
//    }
// }
