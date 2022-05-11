package org.sorapointa.dispatch

import com.google.protobuf.ByteString
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import org.slf4j.LoggerFactory
import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.dispatch.data.ClientCustomConfig
import org.sorapointa.dispatch.data.RegionListClientCustomConfig
import org.sorapointa.dispatch.plugins.*
import org.sorapointa.dispatch.plugins.configureHTTP
import org.sorapointa.dispatch.plugins.configureMonitoring
import org.sorapointa.dispatch.plugins.configureRouting
import org.sorapointa.dispatch.plugins.configureSerialization
import org.sorapointa.proto.QueryCurrRegionHttpRspOuterClass.QueryCurrRegionHttpRsp
import org.sorapointa.proto.QueryRegionListHttpRspOuterClass.QueryRegionListHttpRsp
import org.sorapointa.proto.queryRegionListHttpRsp
import org.sorapointa.proto.regionSimpleInfo
import org.sorapointa.utils.*
import java.io.File
import kotlin.text.toCharArray

internal fun main(): Unit = runBlocking {
    DispatchServer.startDispatch()
}

object DispatchServer {

    var gateServerIp: String = "localhost"
    var gateServerPort: Int = 22101

    internal val client by lazy {
        HttpClient(CIO) {
            install(Logging)
            install(ContentNegotiation) {
                json(prettyJson)
            }
        }
    }

    private fun getEnvironment() = applicationEngineEnvironment {
        val dispatchConfig = DispatchConfig.data
        log = LoggerFactory.getLogger("ktor.application")
        if (dispatchConfig.useSSL) {
            sslConnector(
                keyStore = KeyProvider.getCerts(),
                keyAlias = dispatchConfig.certificationConfig.keyAlias,
                keyStorePassword = { dispatchConfig.certificationConfig.keyStorePassword.toCharArray() },
                privateKeyPassword = { dispatchConfig.certificationConfig.privateKeyPassword.toCharArray() }
            ) {
                host = dispatchConfig.host
                port = dispatchConfig.port
                keyStorePath = File(dispatchConfig.certificationConfig.keyStoreFilePath)
            }
        } else {
            connector {
                host = dispatchConfig.host
                port = dispatchConfig.port
            }
        }
    }

    private fun ApplicationEngineEnvironment.setupApplication() {
        this.application.apply {
            configureSerialization()
            configureStatusPage()
            configureMonitoring()
            configureHTTP()
            configureRouting()
        }
    }

    suspend fun startDispatch(
        gateServerIp: String = "localhost",
        gateServerPort: Int = 22101
    ) {
        this.gateServerIp = gateServerIp
        this.gateServerPort = gateServerPort
        DispatchConfig.init()
        val environment = getEnvironment()
        environment.setupApplication()
        embeddedServer(Netty, environment = environment).start(wait = true)
    }

    // TODO: inline as a member variable and store it in memory
    suspend fun getQueryRegionListHttpRsp(): QueryRegionListHttpRsp {
        val serverList = DispatchConfig.data.servers.map {
            regionSimpleInfo {
                name = it.serverName
                title = it.title
                type = it.serverType
                dispatchUrl = "https://${it.dispatchDomain}/query_cur_region"
            }
        }

        return withContext(Dispatchers.IO) {
            val dispatchSeed = File(configDirectory, "dispatchSeed.bin").readBytes()
            val dispatchKey = File(configDirectory, "dispatchKey.bin").readBytes()

            queryRegionListHttpRsp {
                regionList.addAll(serverList)
                clientSecretKey = ByteString.copyFrom(dispatchSeed)
                enableLoginPc = true
                clientCustomConfigEncrypted = ByteString.copyFrom(
                    networkJson.encodeToString(
                        RegionListClientCustomConfig(
                            sdkEnvironment = 2,
                            showException = false,
                            loadPatch = false,
                            regionConfig = "",
                            regionDispatchType = 0,
                            videoKey = 5578228838233776,
                            downloadMode = 0
                        )
                    ).toByteArray().xor(dispatchKey)
                )
            }

//        val binFile = File(configDirectory, "query_region_list.bin")
//        val queryRegionListHttpRsp = QueryRegionListHttpRsp.parseFrom(binFile.readBytes())
//
//        return queryRegionListHttpRsp.toBuilder()
//            .clearRegionList()
//            .addAllRegionList(serverList)
//            .build()
        }
    }

    /* ktlint-disable max-line-length */
//    private val OFFICIAL_QUERY_CURR_URL = "aHR0cHM6Ly9jbmdmZGlzcGF0Y2gueXVhbnNoZW4uY29tL3F1ZXJ5X2N1cl9yZWdpb24/dmVyc2lvbj1DTlJFTFdpbjIuNi4wJmxhbmc9MiZwbGF0Zm9ybT0zJmJpbmFyeT0xJnRpbWU9NTkwJmNoYW5uZWxfaWQ9MSZzdWJfY2hhbm5lbF9pZD0xJmFjY291bnRfdHlwZT0xJmRpc3BhdGNoU2VlZD0yMjdmYTQ3ZGE4Y2U3ZGNh".decodeBase64String()
    private val QUERY_CURR_DOMAIN = "Y25nZmRpc3BhdGNoLnl1YW5zaGVuLmNvbQ==".decodeBase64String()
    /* ktlint-enable max-line-length */

    suspend fun getQueryCurrRegionHttpRsp(call: ApplicationCall): QueryCurrRegionHttpRsp {

        val queryCurrRegionHttpRsp = QueryCurrRegionHttpRsp.parseFrom(
            call.forwardCall<String>(QUERY_CURR_DOMAIN).decodeBase64Bytes()
        )

        val dispatchSeed = File(configDirectory, "dispatchSeed.bin").readBytes()
        val dispatchKey = File(configDirectory, "dispatchKey.bin").readBytes()

        return queryCurrRegionHttpRsp.toBuilder()
            .setRegionInfo(
                queryCurrRegionHttpRsp.regionInfo.toBuilder()
                    .setGateserverIp(gateServerIp)
                    .setGateserverPort(gateServerPort)
                    .setSecretKey(ByteString.copyFrom(dispatchSeed))
                    .build()
            )
            .setClientSecretKey(ByteString.copyFrom(dispatchSeed))
            .setRegionCustomConfigEncrypted(
                ByteString.copyFrom(
                    networkJson.encodeToString(
                        ClientCustomConfig(
                            codeSwitch = listOf(15, 2410, 2324, 21),
                            coverSwitch = listOf(8, 40),
                            perfReportEnable = false,
                            homeDotPattern = true,
                            homeItemFilter = 20,
                            reportNetDelayConfig = ClientCustomConfig.ReportNetDelayConfigData(
                                openGateServer = true
                            )
                        )
                    ).toByteArray().xor(dispatchKey)
                )
            ).build()

//        val binFile = File(configDirectory, "query_cur_region.bin")
//        val queryCurrRegionHttpRsp = QueryCurrRegionHttpRsp.parseFrom(binFile.readBytes())
//
//        return queryCurrRegionHttpRsp
    }
}

object DispatchConfig : DataFilePersist<DispatchConfig.Data>(
    File(configDirectory, "dispatchConfig.json"), Data()
) {

    @kotlinx.serialization.Serializable
    data class Data(
        var host: String = "0.0.0.0",
        var publicDispatchHost: String = "localhost",
        var port: Int = 443,
        var servers: ArrayList<Server> = arrayListOf(Server()),
        var forwardCommonRequest: Boolean = true,
        // If false, dispatch server will use default config hardcoded in Sorapointa
        var useSSL: Boolean = true,
        var certificationConfig: Certification = Certification()
    )

    @kotlinx.serialization.Serializable
    data class Certification(
        var keyStoreFilePath: String = File(configDirectory, KeyProvider.DEFAULT_CERT_NAME).absPath,
        var keyStore: String = "JKS",
        var keyAlias: String = KeyProvider.DEFAULT_ALIAS,
        var keyStorePassword: String = KeyProvider.DEFAULT_KEY_STORE_PASSWORD,
        var privateKeyPassword: String = KeyProvider.DEFAULT_CERT_PASSWORD
    )

    @kotlinx.serialization.Serializable
    data class Server(
        var serverName: String = "sorapointa_01",
        var title: String = "Sorapointa",
        var serverType: String = "DEV_PUBLIC",
        var dispatchDomain: String = "localhost",
    )
}
