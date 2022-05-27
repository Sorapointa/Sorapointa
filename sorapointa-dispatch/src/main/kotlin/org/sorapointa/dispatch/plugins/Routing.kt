package org.sorapointa.dispatch.plugins

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.sorapointa.dispatch.DispatchConfig
import org.sorapointa.dispatch.DispatchServer
import org.sorapointa.dispatch.data.*
import org.sorapointa.dispatch.events.*
import org.sorapointa.dispatch.util.getAndPost
import org.sorapointa.event.broadcastEvent
import org.sorapointa.utils.SorapointaInternal
import org.sorapointa.utils.i18n
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

@OptIn(SorapointaInternal::class)
internal suspend fun ApplicationCall.handleQueryRegionList() {
    logger.info { "Client ${request.local.host} has queried region list" }
    val packet = DispatchServer.getQueryRegionListHttpRsp()
    QueryRegionListEvent(this, packet).broadcastEvent {
        respondText(it.data.toByteArray().encodeBase64())
    }
}

@OptIn(SorapointaInternal::class)
internal suspend fun ApplicationCall.handleQueryCurRegion() {
    logger.info { "Client ${request.local.host} has queried current region" }
    val packet = DispatchServer.getQueryCurrRegionHttpRsp(this)
    QueryCurrRegionEvent(this, packet).broadcastEvent {
        // We don't need to sign if client has been hooked
        // And we would not to implement this sign algorithm,
        // because it's related with injecting new RSA key,
        // but that is uncessary for now.
        val data = it.data.toByteArray().encodeBase64()
        if (DispatchConfig.data.v28) {
            respond(QueryCurRegionData(data))
        } else {
            respondText(data)
        }
    }
}

internal suspend fun ApplicationCall.handleLogin() {
    val loginAccountRequestData = receive<LoginAccountRequestData>()

    val returnErrorMsg: suspend (String) -> Unit = { msg ->
        LoginAccountResponseEvent(this,
            LoginResultData(-201, msg.i18n())
        ).respond()
    }

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
            returnErrorMsg("dispatch.login.errqor.length.password")
            return@broadcastEvent
        }
        newSuspendedTransaction {
            val account = Account.findOrCreate(name, pwd)
            if (!account.checkPassword(pwd)) {
                returnErrorMsg("dispatch.login.error.password")
                return@newSuspendedTransaction
            }
            val token = account.generateDipatchToken()
            LoginAccountResponseEvent(
                this@handleLogin,
                LoginResultData(
                    returnCode = 0, message = "OK",
                    LoginResultData.VerifyData(
                        LoginResultData.VerifyAccountData(
                            uid = account.userId.value,
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

    val returnErrorMsg: suspend (String) -> Unit = { msg ->
        ComboTokenResponseEvent(this,
            ComboTokenResponseData(-201, msg.i18n())
        ).respond()
    }

    //TODO: hardcode warning
    if (!comboTokenRequestData.signCheck(parameters["region"] == "hk4e_cn")) {
        returnErrorMsg("dispatch.login.error.sign")
        return
    }

    newSuspendedTransaction {
        val account = Account.findById(comboTokenRequestData.data.uid)
        if (account == null) {
            returnErrorMsg("dispatch.login.error.user.notfound")
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
            ComboTokenResponseData(returnCode = 0, message = "OK",
                ComboTokenResponseData.LoginData(
                    accountType = 1u,
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

    val returnErrorMsg: suspend (String) -> Unit = { msg ->
        LoginAccountResponseEvent(
            this@handleVerify,
            LoginResultData(-201, msg.i18n())
        ).respond()
    }

    newSuspendedTransaction {
        val account = Account.findById(verifyData.uid)
        if (account == null) {
            returnErrorMsg("dispatch.login.error.user.notfound")
            return@newSuspendedTransaction
        }
        if (account.getDispatchToken() != verifyData.token) {
            returnErrorMsg("dispatch.login.error.token")
            return@newSuspendedTransaction
        }
        val token = account.generateDipatchToken()
        LoginAccountResponseEvent(
            this@handleVerify,
            LoginResultData(
                returnCode = 0, message = "OK",
                LoginResultData.VerifyData(
                    LoginResultData.VerifyAccountData(
                        uid = account.userId.value,
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
        DOMAIN_SDK_STATIC,
        MdkShieldLoadConfigData(
            returnCode = 0, message = "OK",
            data = MdkShieldLoadConfigData.Data(
                id = 6u,
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
                pushAliasType = 1u,
                enableAnnouncePicPopup = true,
                disableYsdkGuard = true
            )
        )
    ) { GetComboConfigDataEvent(this, it) }
}

internal suspend fun ApplicationCall.handleGetCompareProtocolVersion() {
    forwardCallWithAll(
        DOMAIN_HK4E_SDK,
        CompareProtocolVersionData(
            returnCode = 0, message = "OK",
            data = CompareProtocolVersionData.Data(
                modified = true,
                protocol = CompareProtocolVersionData.Data.Protocol(
                    id = 0u,
                    appId = 4u,
                    language = "en",
                    userProto = "",
                    privProto = "",
                    major = 26u,
                    minimum = 2u,
                    createTime = 0u,
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

internal fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello Sorapointa! The Dispatch Server has been successfully launched!")
        }
    }

    configureNeedHandlerRouting()
    configureHardCodeRouting()
}

// Prevent Code Search

private val DOMAIN_SDK_STATIC = "c2RrLXN0YXRpYy5taWhveW8uY29t".decodeBase64String()
private val DOMAIN_HK4E_SDK = "aGs0ZS1zZGsubWlob3lvLmNvbQ==".decodeBase64String()
private val DOMAIN_HK4E_OS_VERSION = "aGs0ZS1zZGstb3MuaG95b3ZlcnNlLmNvbQ==".decodeBase64String()
private val DOMAIN_WEB_STATIC = "d2Vic3RhdGljLm1paG95by5jb20=".decodeBase64String()
private val ANNOUNCE_URL =
    "aHR0cHM6Ly93ZWJzdGF0aWMubWlob3lvLmNvbS9oazRlL2Fubm91bmNlbWVudC9pbmRleC5odG1s".decodeBase64String()

internal fun Application.configureNeedHandlerRouting() {
    routing {
        get("/query_region_list") {
            call.handleQueryRegionList()
        }

        get("/query_cur_region") {
            call.handleQueryCurRegion()
        }

        route("/hk4e_{region}") {
            route("/combo/granter") {
                route("/login/v2") {
                    post("/login") {
                        call.handleComboLogin()
                    }
                }
                route("/api") {
                    get("/getConfig") {
                        call.handleGetConfig()
                    }
                    post("/compareProtocolVersion") {
                        call.handleGetCompareProtocolVersion()
                    }
                }
            }
            route("/mdk") {
                route("/shield/api") {
                    post("/verify") {
                        call.handleVerify()
                    }
                    get("/loadConfig") {
                        call.handleLoadConfig()
                    }
                    post("/login") {
                        call.handleLogin()
                    }
                }
                getAndPost("/shopwindow/shopwindow/listPriceTier") {
                    // TODO: sniffing some packets and replace the hardcode
                    call.respondText {
                        """{"retcode":0,"message":"OK","data":{"suggest_currency":"USD","tiers":[]}}"""
                    }
                }
                get("/agreement/api/getAgreementInfos") {
                    call.handleGetAgreementInfos()
                }
            }
        }

        get("/combo/box/api/config/sdk/combo") {
            call.handleGetComboData()
        }

//        get("/admin/mi18n/plat_oversea/m{id}/m{id0}-version.json") {
//            if (call.parameters["id"] == call.parameters["id0"]) {
//                call.forwardCallWithAll(DOMAIN_WEB_STATIC, PlatMVersionData(version = 65u))
//                { GetPlatMVersionDataEvent(call, it) }
//            }
//        }

        get("/admin/mi18n/plat_oversea/*/*") {
            call.respondText(call.forwardCall(DOMAIN_WEB_STATIC))
        }

        route("/common/hk4e_{region}") {
            route("/announcement/api") {
                getAndPost("/getAlertPic") {
                    // TODO: sniffing some packets and replace the hardcode
                    call.respondText {
                        """{"retcode":0,"message":"OK","data":{"alert":false,"alert_id":0,"remind":true}}"""
                    }
                }

                getAndPost("/getAnnList") {
                    // TODO: sniffing some packets and replace the hardcode
                    // TODO: Event
                }

                getAndPost("/getAnnContent") {
                    // TODO: sniffing some packets and replace the hardcode
                    // TODO: Event
                }
            }
        }
    }
}

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
    val data = if (DispatchConfig.data.forwardCommonRequest) {
        this.forwardCall(domain)
    } else {
        defaultData
    }
    eventBuilder(data).broadcastEvent {
        this.respond(it.data)
    }
}

internal suspend inline fun <reified T> ApplicationCall.forwardCall(domain: String): T {
    val url = "https://$domain${this.request.uri}"
    logger.debug { "Forwarding request from ${this.request.uri} to $url" }
    val idUrl = url + this.request.queryParameters.formUrlEncode()
    return forwardCache.getOrPut(idUrl) {
        val call = this
        val result: T = DispatchServer.client.request {
            url(url)
            host = domain
            method = call.request.httpMethod
            call.request.userAgent()?.let { userAgent(it) }
            parametersOf(call.request.queryParameters.toMap())
            call.request.headers.forEach { s, strings ->
                if (s.contains(matchHeaders)) header(s, strings)
            }
        }.body()
        logger.debug { "Forwarding result of $url is $result" }
        result
    } as T
}

internal fun Application.configureThirdPartyAuth() {
    routing {
        route("/sorapointa_auth") {
            post("/register") {
                // TODO: For future 3rd party auth
            }
            post("/login") {
                // TODO: For future 3rd party auth
            }
            post("/change_password") {
                // TODO: For future 3rd party auth
            }
        }
    }
}

/* ktlint-disable max-line-length */

internal fun Application.configureHardCodeRouting() {
    routing {
        post("/account/risky/api/check") {
            call.respondText { """{"retcode":0,"message":"OK","data":{"id":"none","action":"ACTION_NONE","geetest":null}}""" }
        }

        post("/data_abtest_api/config/experiment/list") { // AB Test ?? Not sure now
            call.respondText { """{"retcode":0,"success":true,"message":"","data":[{"code":1000,"type":2,"config_id":"14","period_id":"6036_99","version":"1","configs":{"cardType":"old"}}]}""" }
        }

        getAndPost("/log/sdk/upload") {
            call.respondText { """{"code":0}""" }
        }

        getAndPost("/sdk/upload") {
            call.respondText { """{"code":0}""" }
        }

        getAndPost("/sdk/dataUpload") {
            call.respondText { """{"code":0}""" }
        }

        getAndPost("/perf/config/verify") {
            call.respondText { """{"code":0}""" }
        }

        getAndPost("/log") {
            call.respondText { """{"code":0}""" }
        }

        getAndPost("/crash/dataUpload") {
            call.respondText { """{"code":0}""" }
        }
    }
}

/* ktlint-enable max-line-length */
