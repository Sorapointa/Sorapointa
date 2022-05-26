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

/* ktlint-disable max-line-length */
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
private val DOMAIN_HK4E_OS_VERSE = "aGs0ZS1zZGstb3MuaG95b3ZlcnNlLmNvbQ==".decodeBase64String()
private val DOMAIN_WEB_STATIC = "d2Vic3RhdGljLm1paG95by5jb20=".decodeBase64String()
private val ANNOUNCE_URL =
    "aHR0cHM6Ly93ZWJzdGF0aWMubWlob3lvLmNvbS9oazRlL2Fubm91bmNlbWVudC9pbmRleC5odG1s".decodeBase64String()

@OptIn(SorapointaInternal::class)
internal fun Application.configureNeedHandlerRouting() {
    routing {
        get("/query_region_list") {
            logger.info { "Client ${call.request.local.host} has queried region list" }
            val packet = DispatchServer.getQueryRegionListHttpRsp()
            QueryRegionListEvent(call, packet).broadcastEvent { call.respondText(it.data.toByteArray().encodeBase64()) }
        }

        get("/query_cur_region") {
            logger.info { "Client ${call.request.local.host} has queried current region" }
            val packet = DispatchServer.getQueryCurrRegionHttpRsp(call)
            QueryCurrRegionEvent(call, packet).broadcastEvent {
                call.respondText(it.data.toByteArray().encodeBase64())
            }
        }

        route("/hk4e_{region}") {
            route("/combo/granter") {
                route("/login/v2") {
                    post("/login") {
                        val comboTokenRequestData = call.receive<ComboTokenRequestData>()
                        if (!comboTokenRequestData.signCheck(call.parameters["region"] == "hk4e_cn")) {
                            ComboTokenResponseEvent(
                                call,
                                ComboTokenResponseData(-201, "dispatch.login.error.sign".i18n())
                            ).respond()
                            return@post
                        }
                        newSuspendedTransaction {
                            val account = Account.findById(comboTokenRequestData.data.uid)
                            if (account == null) {
                                LoginAccountResponseEvent(
                                    call,
                                    LoginResultData(-201, "dispatch.login.error.user.notfound".i18n())
                                ).respond()
                                return@newSuspendedTransaction
                            }
                            if (account.dispatchToken != comboTokenRequestData.data.token) {
                                ComboTokenResponseEvent(
                                    call,
                                    ComboTokenResponseData(-201, "dispatch.login.error.token".i18n())
                                ).respond()
                                return@newSuspendedTransaction
                            }
                            val comboId = account.getComboIdOrGenerate()
                            val comboToken = account.getComboTokenOrGenerate()
                            ComboTokenResponseEvent(
                                call,
                                ComboTokenResponseData(
                                    returnCode = 0, message = "OK",
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
                }
                route("/api") {
                    get("/getConfig") {
                        call.forwardCallWithAll(
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
                        ) { GetComboConfigDataEvent(call, it) }
                    }
                    post("/compareProtocolVersion") {
                        call.forwardCallWithAll(
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
                        ) { GetCompareProtocolVersionDataEvent(call, it) }
                    }
                }
            }
            route("/mdk") {
                route("/shield/api") {
                    post("/verify") {
                        val verifyData = call.receive<VerifyTokenRequestData>()
                        newSuspendedTransaction {
                            val account = Account.findById(verifyData.uid)
                            if (account == null) {
                                LoginAccountResponseEvent(
                                    call,
                                    LoginResultData(-201, "dispatch.login.error.user.notfound".i18n())
                                ).respond()
                                return@newSuspendedTransaction
                            }
                            if (account.dispatchToken != verifyData.token) {
                                LoginAccountResponseEvent(
                                    call,
                                    LoginResultData(-201, "dispatch.login.error.token".i18n())
                                ).respond()
                                return@newSuspendedTransaction
                            }
                            val token = account.getDispatchTokenOrGenerate()
                            LoginAccountResponseEvent(
                                call,
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
                    get("/loadConfig") {
                        call.forwardCallWithAll(
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
                        ) { GetMdkShieldLoadConfigDataEvent(call, it) }
                    }
                    post("/login") {
                        val loginAccountRequestData = call.receive<LoginAccountRequestData>()
                        LoginAccountRequestEvent(call, loginAccountRequestData).broadcastEvent {
                            // TODO: Introduce a better solution for account system
                            val split = it.data.account.split(":")
                            if (split.size != 2) {
                                LoginAccountResponseEvent(
                                    call,
                                    LoginResultData(-201, "dispatch.login.error.split".i18n())
                                ).respond()
                                return@broadcastEvent
                            }
                            val name = split[0]
                            val pwd = split[1]
                            if (name.length !in 3..16) {
                                LoginAccountResponseEvent(
                                    call,
                                    LoginResultData(-201, "dispatch.login.error.length.name".i18n())
                                ).respond()
                                return@broadcastEvent
                            }
                            if (pwd.length !in 8..32) {
                                LoginAccountResponseEvent(
                                    call,
                                    LoginResultData(-201, "dispatch.login.error.length.password".i18n())
                                ).respond()
                                return@broadcastEvent
                            }
                            newSuspendedTransaction {
                                val account = Account.findOrCreate(name, pwd)
                                if (!account.checkPassword(pwd)) {
                                    LoginAccountResponseEvent(
                                        call,
                                        LoginResultData(-201, "dispatch.login.error.password".i18n())
                                    ).respond()
                                    return@newSuspendedTransaction
                                }
                                val token = account.getDispatchTokenOrGenerate()
                                LoginAccountResponseEvent(
                                    call,
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
                }
                getAndPost("/shopwindow/shopwindow/listPriceTier") {
                    // TODO: sniffing some packets and replace the hardcode
                    call.respondText { """{"retcode":0,"message":"OK","data":{"suggest_currency":"USD","tiers":[]}}""" }
                }
                get("/agreement/api/getAgreementInfos") {
                    call.forwardCallWithAll(
                        DOMAIN_HK4E_OS_VERSE,
                        AgreementData(
                            returnCode = 0, message = "OK",
                            data = AgreementData.Data(
                                marketingAgreements = arrayListOf()
                            )
                        )
                    ) // TODO: not sure so far
                    { GetAgreementDataEvent(call, it) }
                }
            }
        }

        get("/combo/box/api/config/sdk/combo") {
            call.forwardCallWithAll(
                DOMAIN_SDK_STATIC,
                ComboData(
                    returnCode = 0, message = "OK",
                    data = ComboData.Data(
                        values = mapOf("modify_real_name_other_verify" to "true")
                    )
                )
            ) { GetComboDataEvent(call, it) }
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
                    call.respondText { """{"retcode":0,"message":"OK","data":{"alert":false,"alert_id":0,"remind":true}}""" }
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
