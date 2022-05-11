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
import org.sorapointa.dispatch.DispatchConfig
import org.sorapointa.dispatch.DispatchServer
import org.sorapointa.dispatch.data.*
import org.sorapointa.dispatch.events.*
import org.sorapointa.dispatch.util.getAndPost
import org.sorapointa.event.Event
import org.sorapointa.event.EventManager.broadcastEvent
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

internal fun Application.configureNeedHandlerRouting() {
    routing {
        get("/query_region_list") {
            logger.info { "Client ${call.request.local.host} has queried region list" }
            val packet = DispatchServer.getQueryRegionListHttpRsp()
            QueryRegionListEvent(call, packet).broadcastEvent {
                call.respondText(it.queryRegionListHttpRsp.toByteArray().encodeBase64())
            }
        }

        get("/query_cur_region") {
            logger.info { "Client ${call.request.local.host} has queried current region" }
            val packet = DispatchServer.getQueryCurrRegionHttpRsp(call)
            QueryCurrRegionEvent(call, packet).broadcastEvent {
                call.respondText(it.queryCurrRegionHttpRsp.toByteArray().encodeBase64())
            }
        }

        route("/hk4e_{region}") {
            route("/combo/granter/api") {
                get("/getConfig") {
                    call.forwardCallWithAll(
                        DOMAIN_SDK_STATIC,
                        ComboConfigData(
                            returnCode = 0,
                            message = "OK",
                            data = ComboConfigData.Data(
                                protocol = false,
                                qrEnabled = false,
                                logLevel = "INFO",
                                announceUrl = """https://webstatic.mihoyo.com/hk4e/announcement/index.html""",
                                pushAliasType = 1,
                                enableAnnouncePicPopup = true,
                                disableYsdkGuard = true
                            )
                        )
                    ) { GetComboConfigDataEvent(call, it) }?.also { call.respond(it.comboConfigData) }
                }
                post("/compareProtocolVersion") {
                    call.forwardCallWithAll(
                        DOMAIN_HK4E_SDK,
                        CompareProtocolVersionData(
                            returnCode = 0,
                            message = "OK",
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
                    ) { GetCompareProtocolVersionDataEvent(call, it) }
                        ?.also { call.respond(it.compareProtocolVersionData) }
                }
            }
            route("/mdk") {
                get("/shield/api/loadConfig") {
                    call.forwardCallWithAll(
                        DOMAIN_SDK_STATIC,
                        MdkShieldLoadConfigData(
                            returnCode = 0,
                            message = "OK",
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
                    ) { GetMdkShieldLoadConfigDataEvent(call, it) }?.also { call.respond(it.mdkShieldLoadConfigData) }
                }
                getAndPost("/shopwindow/shopwindow/listPriceTier") {
                    // TODO: sniffing some packets and replace the hardcode
                    call.respondText { """{"retcode":0,"message":"OK","data":{"suggest_currency":"USD","tiers":[]}}""" }
                }
                get("/agreement/api/getAgreementInfos") {
                    call.forwardCallWithAll(
                        DOMAIN_HK4E_OS_VERSE,
                        AgreementData(
                            returnCode = 0,
                            message = "OK",
                            data = AgreementData.Data(
                                marketingAgreements = arrayListOf()
                            )
                        )
                    ) // TODO: not sure so far
                    { GetAgreementDataEvent(call, it) }?.also { call.respond(it.agreementData) }
                }
            }
        }

        get("/combo/box/api/config/sdk/combo") {
            call.forwardCallWithAll(
                DOMAIN_SDK_STATIC,
                ComboData(
                    returnCode = 0,
                    message = "OK",
                    data = ComboData.Data(
                        values = mapOf("modify_real_name_other_verify" to "true")
                    )
                )
            ) { GetComboDataEvent(call, it) }?.also { call.respond(it.comboData) }
        }

        get("/admin/mi18n/plat_oversea/m{id}/m{id0}*") {
            if (call.parameters["id"] == call.parameters["id0"]) {
                call.forwardCallWithAll(DOMAIN_WEB_STATIC, PlatMVersionData(version = 65)) { GetPlatMVersionDataEvent(call, it) }?.also { call.respond(it.platMVersionData) }
            }
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

internal suspend inline fun <reified T, reified E : Event> ApplicationCall.forwardCallWithAll(
    domain: String,
    defaultData: T,
    eventBuilder: (T) -> E
): E? {
    val data = if (DispatchConfig.data.forwardCommonRequest) {
        this.forwardCall(domain)
    } else {
        defaultData
    }
    val event = eventBuilder(data)
    val isCancelled = broadcastEvent(event)
    return if (!isCancelled) {
        event
    } else {
        null
    }
}

internal suspend inline fun <reified T> ApplicationCall.forwardCall(domain: String): T {
    val url = "https://$domain${this.request.uri}"
    val idUrl = url + this.request.queryParameters.formUrlEncode()
    return if (!forwardCache.containsKey(idUrl)) {
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
        forwardCache[idUrl] = result as Any
        result
    } else {
        forwardCache[idUrl]!! as T
    }
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
