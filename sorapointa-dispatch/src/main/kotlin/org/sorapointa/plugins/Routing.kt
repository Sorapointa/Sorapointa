package org.sorapointa.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import mu.KotlinLogging
import org.sorapointa.util.getAndPost

private val logger = KotlinLogging.logger {}

/* ktlint-disable max-line-length */
fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello Sorapointa! The Dispatch Server has been successfully launched!")
        }

        get("/authentication/type") {
            call.respondText("AuthenticationHandler")
        }

        get("/authentication/login") {
            call.respondText("Authentication is not available with the default authentication method")
        }

        get("/authentication/register") {
            call.respondText("Authentication is not available with the default authentication method")
        }

        get("/authentication/change_password") {
            call.respondText("Authentication is not available with the default authentication method")
        }

        get("/query_region_list") {
            logger.info { "Client ${call.request.local.host} has queried region list" }
            // TODO: Event
        }

        get("/hk4e_global/mdk/agreement/api/getAgreementInfos") {
            call.respondText { """{"retcode":0,"message":"OK","data":{"marketing_agreements":[]}}""" }
        }

        getAndPost("/hk4e_global/combo/granter/api/compareProtocolVersion") {
            call.respondText { """{"retcode":0,"message":"OK","data":{"modified":true,"protocol":{"id":0,"app_id":4,"language":"en","user_proto":"","priv_proto":"","major":7,"minimum":0,"create_time":"0","teenager_proto":"","third_proto":""}}}""" }
        }

        getAndPost("/common/hk4e_global/announcement/api/getAlertPic") {
            call.respondText { """{"retcode":0,"message":"OK","data":{"alert":false,"alert_id":0,"remind":true}}""" }
        }

        getAndPost("/common/hk4e_global/announcement/api/getAnnList") {
            // TODO: Event
        }

        getAndPost("/common/hk4e_global/announcement/api/getAnnContent") {
            // TODO: Event
        }

        getAndPost("/hk4e_global/mdk/shopwindow/shopwindow/listPriceTier") {
            call.respondText { """{"retcode":0,"message":"OK","data":{"suggest_currency":"USD","tiers":[]}}""" }
        }

        post("/account/risky/api/check") {
            call.respondText { """{"retcode":0,"message":"OK","data":{"id":"none","action":"ACTION_NONE","geetest":null}}""" }
        }

        get("/combo/box/api/config/sdk/combo") {
            call.respondText { """{"retcode":0,"message":"OK","data":{"vals":{"disable_email_bind_skip":"false","email_bind_remind_interval":"7","email_bind_remind":"true"}}}""" }
        }

        get("/hk4e_global/combo/granter/api/getConfig") {
            call.respondText { """{"retcode":0,"message":"OK","data":{"protocol":true,"qr_enabled":false,"log_level":"INFO","announce_url":"https://webstatic-sea.hoyoverse.com/hk4e/announcement/index.html?sdk_presentation_style=fullscreen\u0026sdk_screen_transparent=true\u0026game_biz=hk4e_global\u0026auth_appid=announcement\u0026game=hk4e#/","push_alias_type":2,"disable_ysdk_guard":false,"enable_announce_pic_popup":true}}""" }
        }

        get("/hk4e_global/mdk/shield/api/loadConfig") {
            call.respondText { """{"retcode":0,"message":"OK","data":{"id":6,"game_key":"hk4e_global","client":"PC","identity":"I_IDENTITY","guest":false,"ignore_versions":"","scene":"S_NORMAL","name":"原神海外","disable_regist":false,"enable_email_captcha":false,"thirdparty":["fb","tw"],"disable_mmt":false,"server_guest":false,"thirdparty_ignore":{"tw":"","fb":""},"enable_ps_bind_account":false,"thirdparty_login_configs":{"tw":{"token_type":"TK_GAME_TOKEN","game_token_expires_in":604800},"fb":{"token_type":"TK_GAME_TOKEN","game_token_expires_in":604800}}}}""" }
        }

        post("/data_abtest_api/config/experiment/list") {
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

        get("/admin/mi18n/plat_oversea/m202003048/m202003048-version.json") {
            call.respondText { """{"version":51}""" }
        }

        get("/gacha") {
            call.respondHtml(HttpStatusCode.OK) {
                this.head {
                    title {
                        +"Gacha"
                    }
                }
                body {
                    h1 {
                        +"Gacha"
                    }
                }
            }
        }
    }
}
/* ktlint-enable max-line-length */
