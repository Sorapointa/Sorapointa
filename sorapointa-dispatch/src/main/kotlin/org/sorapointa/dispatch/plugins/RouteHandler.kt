package org.sorapointa.dispatch.plugins

import com.google.protobuf.kotlin.toByteString
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import mu.KotlinLogging
import org.jetbrains.annotations.PropertyKey
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.sorapointa.dispatch.BUNDLE
import org.sorapointa.dispatch.DispatchBundle
import org.sorapointa.dispatch.DispatchConfig
import org.sorapointa.dispatch.DispatchServer
import org.sorapointa.dispatch.data.*
import org.sorapointa.dispatch.events.*
import org.sorapointa.event.broadcastEvent
import org.sorapointa.proto.QueryCurrRegionHttpRspOuterClass.QueryCurrRegionHttpRsp
import org.sorapointa.proto.QueryRegionListHttpRspOuterClass.QueryRegionListHttpRsp
import org.sorapointa.proto.queryCurrRegionHttpRsp
import org.sorapointa.proto.queryRegionListHttpRsp
import org.sorapointa.proto.regionSimpleInfo
import org.sorapointa.utils.SorapointaInternal
import org.sorapointa.utils.crypto.RSAKey
import org.sorapointa.utils.crypto.parseToRSAKey
import org.sorapointa.utils.networkJson
import org.sorapointa.utils.xor
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

private val DOMAIN_SDK_STATIC = "c2RrLW9zLXN0YXRpYy5ob3lvdmVyc2UuY29t".decodeBase64String()
private val DOMAIN_HK4E_OS_VERSION = "aGs0ZS1zZGstb3MuaG95b3ZlcnNlLmNvbQ==".decodeBase64String()
private val DOMAIN_WEB_STATIC = "d2Vic3RhdGljLXNlYS5ob3lvdmVyc2UuY29t".decodeBase64String()
private val ANNOUNCE_URL =
    "aHR0cHM6Ly93ZWJzdGF0aWMtc2VhLmhveW92ZXJzZS5jb20vaGs0ZS9hbm5vdW5jZW1lbnQvaW5kZXguaHRtbA==".decodeBase64String()
private val QUERY_CURR_DOMAIN = "Y25nZmRpc3BhdGNoLnl1YW5zaGVuLmNvbQ==".decodeBase64String()

private val serverList = DispatchConfig.data.servers.map {
    regionSimpleInfo {
        name = it.serverName
        title = it.title
        type = it.serverType
        dispatchUrl = "https://${it.dispatchDomain}/query_cur_region"
    }
}

private suspend fun getQueryRegionListHttpRsp(host: String): QueryRegionListHttpRsp {
    val ec2b = newSuspendedTransaction {
        DispatchKeyData.getOrGenerate(host)
    }

    val dispatchSeed = ec2b.seed
    val dispatchKey = ec2b.key

    return queryRegionListHttpRsp {
        regionList.addAll(serverList)
        clientSecretKey = dispatchSeed.toByteString()
        enableLoginPc = true
        clientCustomConfigEncrypted = networkJson.encodeToString(
            DispatchConfig.data.regionListClientCustomConfig
        ).toByteArray().xor(dispatchKey).toByteString()
    }
}

@SorapointaInternal var currentRegionRsp = CompletableDeferred<QueryCurrRegionHttpRsp>()

private val dispatchRSAKey: RSAKey? = DispatchConfig.data.requestSetting.rsaPrivateKey.parseToRSAKey()
private val dispatchPublicRSAKey: RSAKey? = DispatchConfig.data.requestSetting.rsaPublicKey.parseToRSAKey()

suspend fun getCurrentRegionHttpRsp(call: ApplicationCall? = null): QueryCurrRegionHttpRsp {
    val requestSetting = DispatchConfig.data.requestSetting
    return if (requestSetting.forwardQueryCurrentRegion) {
        val forwardResult = if (!requestSetting.usingCurrentRegionUrlHardcode && call != null) {
            call.forwardCall(QUERY_CURR_DOMAIN)
        } else {
            DispatchServer.client.get(requestSetting.queryCurrentRegionHardcode)
        }.bodyAsText()
        if (!requestSetting.v28CurrentRegionForwardFormat || dispatchRSAKey == null) {
            logger.debug { "QueryCurrentRegion Result: $forwardResult" }
            QueryCurrRegionHttpRsp.parseFrom(forwardResult.decodeBase64Bytes())
        } else {
            val query: QueryCurrentRegionData = networkJson.decodeFromString(forwardResult)
            logger.debug { "QueryCurrentRegion Result: $query" }
            with(dispatchRSAKey) {
                val decryptResult = query.content.decodeBase64Bytes().decrypt()
                val signVerifyResult = if (requestSetting.enableSignVerify) {
                    dispatchPublicRSAKey?.run {
                        decryptResult.signVerify(query.sign.decodeBase64Bytes())
                    } ?: false
                } else true // If disable signVerify, return true
                logger.debug { "QueryCurrentRegion Decrypted Result: ${decryptResult.encodeBase64()}" }
                if (signVerifyResult) {
                    QueryCurrRegionHttpRsp.parseFrom(decryptResult)
                } else {
                    error("QueryCurrentRegion sign verify failed")
                }
            }
        }
    } else queryCurrRegionHttpRsp {}
}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun ApplicationCall.forwardQueryCurrentRegionHttpRsp(): QueryCurrRegionHttpRsp {

    val requestSetting = DispatchConfig.data.requestSetting

    val queryCurrentRegionHttpRsp = if (currentRegionRsp.isCompleted) {
        currentRegionRsp.getCompleted()
    } else {
        getCurrentRegionHttpRsp(this).also {
            currentRegionRsp.complete(it)
        }
    }

    val ec2b = newSuspendedTransaction {
        DispatchKeyData.getOrGenerate(host)
    }

    val dispatchSeed = ec2b.seed
    val dispatchKey = ec2b.key

    return queryCurrentRegionHttpRsp.toBuilder()
        .setRegionInfo(
            queryCurrentRegionHttpRsp.regionInfo.toBuilder()
                .setGateserverIp(DispatchConfig.data.gateServerIp)
                .setGateserverPort(DispatchConfig.data.gateServerPort)
                .setSecretKey(dispatchSeed.toByteString())
                .build()
        ).apply {
            if (requestSetting.currentRegionContainsCustomClientConfig) {
                this.regionCustomConfigEncrypted = networkJson.encodeToString(
                    DispatchConfig.data.clientCustomConfig
                ).toByteArray().xor(dispatchKey).toByteString()
            }
        }
        .setClientSecretKey(dispatchSeed.toByteString())
        .build()
}

// --- Route Handler ---

internal suspend fun ApplicationCall.handleQueryRegionList() {
    logger.info { "Client $host has queried region list" }
    QueryRegionListEvent(this, getQueryRegionListHttpRsp(host)).broadcastEvent {
        respondText(it.data.toByteArray().encodeBase64())
    }
}

internal suspend fun ApplicationCall.handleQueryCurrentRegion() {
    logger.info { "Client ${request.local.host} has queried current region" }
    val packet = this.forwardQueryCurrentRegionHttpRsp()
    QueryCurrentRegionEvent(this, packet).broadcastEvent {
        // We don't need to sign if client has been hooked
        // And we would not to implement this sign algorithm,
        // because it's related with injecting new RSA key,
        // but that is uncessary for now.
        val data = it.data.toByteArray().encodeBase64()
        if (DispatchConfig.data.requestSetting.v28CurrentRegionForwardFormat) {
            respond(QueryCurrentRegionData(data))
        } else {
            respondText(data)
        }
    }
}

internal suspend fun ApplicationCall.handleLogin() {
    val loginAccountRequestData = receive<LoginAccountRequestData>()

    suspend fun returnErrorMsg(@PropertyKey(resourceBundle = BUNDLE) msgKey: String) = LoginAccountResponseEvent(
        this, LoginResultData(-201, DispatchBundle.message(msgKey))
    ).respond()

    LoginAccountRequestEvent(this, loginAccountRequestData).broadcastEvent {
        // TODO: Introduce a better solution for account system
        val split = it.data.account.split(":")
        if (split.size != 2) {
            returnErrorMsg("dispatch.login.error.split")
            return@broadcastEvent
        }
        val name = split[0]
        val pwd = split[1]
        if (name.length !in 3..16) {
            returnErrorMsg("dispatch.login.error.length.name")
            return@broadcastEvent
        }
        if (pwd.length !in 8..32) {
            returnErrorMsg("dispatch.login.error.length.password")
            return@broadcastEvent
        }
        newSuspendedTransaction {
            val account = Account.findOrCreate(name, pwd)
            if (!account.checkPassword(pwd)) {
                returnErrorMsg("dispatch.login.error.password")
                return@newSuspendedTransaction
            }
            val token = account.generateDispatchToken()
            LoginAccountResponseEvent(
                this@handleLogin,
                LoginResultData(
                    returnCode = 0, message = "OK",
                    LoginResultData.VerifyData(
                        LoginResultData.VerifyAccountData(
                            uid = account.id.value,
                            token = token,
                            email = account.email ?: name
                        )
                    )
                )
            ).respond()
        }
    }
}

internal suspend fun ApplicationCall.handleComboLogin() {
    val comboTokenRequestData = receive<ComboTokenRequestData>()

    suspend fun returnErrorMsg(@PropertyKey(resourceBundle = BUNDLE) msgKey: String) = ComboTokenResponseEvent(
        this@handleComboLogin, ComboTokenResponseData(-201, DispatchBundle.message(msgKey))
    ).respond()

    // TODO: hardcode warning
    if (!comboTokenRequestData.signCheck(parameters["region"] == "cn")) {
        returnErrorMsg("dispatch.login.error.sign")
        return
    }

    newSuspendedTransaction {
        val account = Account.findById(comboTokenRequestData.data.uid)
        if (account == null) {
            returnErrorMsg("user.notfound")
            return@newSuspendedTransaction
        }
        if (account.getDispatchToken() != comboTokenRequestData.data.token) {
            returnErrorMsg("dispatch.login.error.token")
            return@newSuspendedTransaction
        }
        val comboId = account.getComboIdOrGenerate()
        val comboToken = account.generateComboToken()
        ComboTokenResponseEvent(
            this@handleComboLogin,
            ComboTokenResponseData(
                returnCode = 0, message = "OK",
                ComboTokenResponseData.LoginData(
                    accountType = 1,
                    comboId = comboId,
                    comboToken = comboToken,
                    data = ComboTokenResponseData.LoginData.LoginGuestData(guest = false),
                    heartbeat = false,
                    openId = comboTokenRequestData.data.uid,
                )
            )
        ).respond()
    }
}

internal suspend fun ApplicationCall.handleVerify() {
    val verifyData = receive<VerifyTokenRequestData>()

    suspend fun returnErrorMsg(@PropertyKey(resourceBundle = BUNDLE) msgKey: String) = LoginAccountResponseEvent(
        this@handleVerify, LoginResultData(-201, DispatchBundle.message(msgKey))
    ).respond()

    newSuspendedTransaction {
        val account = Account.findById(verifyData.uid)
        if (account == null) {
            returnErrorMsg("user.notfound")
            return@newSuspendedTransaction
        }
        if (account.getDispatchToken() != verifyData.token) {
            returnErrorMsg("dispatch.login.error.token")
            return@newSuspendedTransaction
        }
        val token = account.generateDispatchToken()
        LoginAccountResponseEvent(
            this@handleVerify,
            LoginResultData(
                returnCode = 0, message = "OK",
                LoginResultData.VerifyData(
                    LoginResultData.VerifyAccountData(
                        uid = account.id.value,
                        token = token,
                        email = account.email
                    )
                )
            )
        ).respond()
    }
}

internal suspend fun ApplicationCall.handleLoadConfig() {
    forwardCallWithAll(
        DOMAIN_HK4E_OS_VERSION,
        MdkShieldLoadConfigData(
            returnCode = 0, message = "OK",
            data = MdkShieldLoadConfigData.Data(
                id = 6,
                gameKey = "sora",
                client = "PC",
                identity = "I_IDENTITY",
                guest = false,
                ignoreVersions = "",
                scene = "S_ACCOUNT",
                name = "Sorapointa",
                disableRegist = false,
                enableEmailCaptcha = true,
                thirdParty = arrayListOf(),
                disableMmt = true,
                serverGuest = false,
                thirdPartyIgnore = mapOf(),
                thirdPartyLoginConfigs = mapOf(),
                enablePsBindAccount = false
            )
        )
    ) { GetMdkShieldLoadConfigDataEvent(this, it) }
}

internal suspend fun ApplicationCall.handleGetConfig() {
    forwardCallWithAll(
        DOMAIN_SDK_STATIC,
        ComboConfigData(
            returnCode = 0, message = "OK",
            data = ComboConfigData.Data(
                protocol = false,
                qrEnabled = false,
                logLevel = "INFO",
                announceUrl = ANNOUNCE_URL.decodeBase64String(),
                pushAliasType = 1,
                enableAnnouncePicPopup = true,
                disableYsdkGuard = true
            )
        )
    ) { GetComboConfigDataEvent(this, it) }
}

internal suspend fun ApplicationCall.handleGetCompareProtocolVersion() {
    forwardCallWithAll(
        DOMAIN_HK4E_OS_VERSION,
        CompareProtocolVersionData(
            returnCode = 0, message = "OK",
            data = CompareProtocolVersionData.Data(
                modified = true,
                protocol = CompareProtocolVersionData.Data.Protocol(
                    id = 0,
                    appId = 4,
                    language = "en",
                    userProto = "",
                    privProto = "",
                    major = 26,
                    minimum = 2,
                    createTime = 0,
                    teenagerProto = "",
                    thirdProto = ""
                )
            )
        )
    ) { GetCompareProtocolVersionDataEvent(this, it) }
}

internal suspend fun ApplicationCall.handleGetAgreementInfos() {
    forwardCallWithAll(
        DOMAIN_HK4E_OS_VERSION,
        AgreementData(
            returnCode = 0, message = "OK",
            data = AgreementData.Data(
                marketingAgreements = arrayListOf()
            )
        )
    ) // TODO: not sure so far
    { GetAgreementDataEvent(this, it) }
}

internal suspend fun ApplicationCall.handleGetComboData() {
    forwardCallWithAll(
        DOMAIN_SDK_STATIC,
        ComboData(
            returnCode = 0, message = "OK",
            data = ComboData.Data(
                values = mapOf("modify_real_name_other_verify" to "true")
            )
        )
    ) { GetComboDataEvent(this, it) }
}

internal suspend fun ApplicationCall.handleM18n() {
    respondText(forwardCall(DOMAIN_WEB_STATIC))
}

// --- Tool Method ---

internal val forwardCache = ConcurrentHashMap<String, Any>()
internal val matchHeaders = Regex("^[xX]-")

internal suspend inline fun <reified T : Any> DispatchDataEvent<T>.respond() {
    this.broadcastEvent {
        this.call.respond(it.data)
        logger.debug { "Respond data ${it.data}" }
    }
}

internal suspend inline fun <reified T : Any> ApplicationCall.forwardCallWithAll(
    domain: String,
    defaultData: T,
    eventBuilder: (T) -> DispatchDataEvent<T>
) {
    val data = if (DispatchConfig.data.requestSetting.forwardCommonRequest) {
        this.forwardCall(domain)
    } else {
        defaultData
    }
    eventBuilder(data).broadcastEvent {
        this.respond(it.data)
    }
}

internal suspend inline fun <reified T> ApplicationCall.forwardCall(
    domain: String
): T {
    val url = "https://${request.headers["original"] ?: domain}${request.uri}"
    logger.debug { "Forwarding request from ${request.uri} to $url" }
    val idUrl = url + request.queryParameters.formUrlEncode()
    return forwardCache.getOrPut(idUrl) {
        val call = this
        val request = DispatchServer.client.request {
            url(url)
            host = domain
            method = call.request.httpMethod
            if (call.request.httpMethod == HttpMethod.Post) {
                setBody(call.receive<JsonElement>())
            }
            call.request.userAgent()?.let { userAgent(it) }
            parametersOf(call.request.queryParameters.toMap())
            contentType(ContentType.Application.Json)
            call.request.headers.forEach { s, strings ->
                if (s.contains(matchHeaders)) header(s, strings)
            }
        }
        val result: T = if (T::class == String::class) request.bodyAsText() as T else request.body()
        logger.debug { "Forwarding result of $url is $result" }
        result
    } as T
}

private val ApplicationCall.host
    get() = request.origin.remoteHost
