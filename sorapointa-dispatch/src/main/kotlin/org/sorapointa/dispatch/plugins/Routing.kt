package org.sorapointa.dispatch.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.sorapointa.dispatch.utils.getAndPost

internal fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello Sorapointa! The dispatch server has been successfully launched!")
        }
    }

    configureNeedHandlerRouting()
    configureHardCodeRouting()
}

// Prevent Code Search

internal fun Application.configureNeedHandlerRouting() {
    routing {
        get("/query_region_list") {
            call.handleQueryRegionList()
        }

        get("/query_cur_region") {
            call.handleQueryCurrentRegion()
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
            call.handleM18n()
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
