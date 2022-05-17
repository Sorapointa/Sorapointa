package org.sorapointa.dispatch

import com.google.protobuf.ByteString
import com.google.protobuf.kotlin.toByteString
import com.password4j.types.Argon2
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
import org.sorapointa.data.provider.DatabaseConfig
import org.sorapointa.data.provider.DatabaseManager
import org.sorapointa.dispatch.data.AccountTable
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
import org.sorapointa.utils.crypto.randomByteArray
import org.sorapointa.utils.encoding.hex
import java.io.File
import kotlin.text.toCharArray

@OptIn(SorapointaInternal::class)
internal fun main(): Unit = runBlocking {
    DispatchServer.startDispatch(isIndependent = true)
}

object DispatchServer {

    internal val client by lazy {
        HttpClient(CIO) {
//            install(Logging)
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
                keyAlias = dispatchConfig.certification.keyAlias,
                keyStorePassword = { dispatchConfig.certification.keyStorePassword.toCharArray() },
                privateKeyPassword = { dispatchConfig.certification.privateKeyPassword.toCharArray() }
            ) {
                host = dispatchConfig.host
                port = dispatchConfig.port
                keyStorePath = File(dispatchConfig.certification.keyStoreFilePath)
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

    @SorapointaInternal
    suspend fun startDispatch(isIndependent: Boolean = false) {
        DispatchConfig.init()
        if (isIndependent) {
            I18nConfig.init()
            I18nManager.registerLanguagesDirectory(languagesDirectory)
            DatabaseConfig.init()
            DatabaseManager.loadDatabase()
        }
        DatabaseManager.loadTables(AccountTable)
        val environment = getEnvironment()
        environment.setupApplication()
        embeddedServer(Netty, environment = environment).start(wait = true)
    }

    // TODO: inline as a member variable and store it in memory
    @SorapointaInternal
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
                clientCustomConfigEncrypted = networkJson.encodeToString(
                    // TODO: Extract to be the event and config
                    RegionListClientCustomConfig(
                        sdkEnvironment = 2u,
                        showException = false,
                        loadPatch = false,
                        regionConfig = "",
                        regionDispatchType = 0u,
                        videoKey = 5578228838233776,
                        downloadMode = 0u
                    )
                ).toByteArray().xor(dispatchKey).toByteString()
            }

        }
    }

    private val QUERY_CURR_DOMAIN = "Y25nZmRpc3BhdGNoLnl1YW5zaGVuLmNvbQ==".decodeBase64String()

    @SorapointaInternal
    suspend fun getQueryCurrRegionHttpRsp(call: ApplicationCall): QueryCurrRegionHttpRsp {

        val queryCurrRegionHttpRsp = QueryCurrRegionHttpRsp.parseFrom(
            (if (DispatchConfig.data.forwardQueryCurrRegion) call.forwardCall(QUERY_CURR_DOMAIN)
            else client.get(DispatchConfig.data.queryCurrRegionHardcode).bodyAsText()).decodeBase64Bytes()
        )

        val dispatchSeed = File(configDirectory, "dispatchSeed.bin").readBytes()
        val dispatchKey = File(configDirectory, "dispatchKey.bin").readBytes()

        return queryCurrRegionHttpRsp.toBuilder()
            .setRegionInfo(
                queryCurrRegionHttpRsp.regionInfo.toBuilder()
                    .setGateserverIp(DispatchConfig.data.gateServerIp)
                    .setGateserverPort(DispatchConfig.data.gateServerPort)
                    .setSecretKey(ByteString.copyFrom(dispatchSeed))
                    .build()
            )
            .setClientSecretKey(ByteString.copyFrom(dispatchSeed))
            .setRegionCustomConfigEncrypted(
                networkJson.encodeToString(
                    // TODO: Extract to be the event and config
                    ClientCustomConfig(
                        codeSwitch = listOf(15u, 2410u, 2324u, 21u),
                        coverSwitch = listOf(8u, 40u),
                        perfReportEnable = false,
                        homeDotPattern = true,
                        homeItemFilter = 20u,
                        reportNetDelayConfig = ClientCustomConfig.ReportNetDelayConfigData(
                            openGateServer = true
                        )
                    )
                ).toByteArray().xor(dispatchKey).toByteString()
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

    /* ktlint-disable max-line-length */
    private val QUERY_CURR_HARDCODE_DEFAULT = "aHR0cHM6Ly9jbmdmZGlzcGF0Y2gueXVhbnNoZW4uY29tL3F1ZXJ5X2N1cl9yZWdpb24/dmVyc2lvbj1DTlJFTFdpbjIuNi4wJmxhbmc9MiZwbGF0Zm9ybT0zJmJpbmFyeT0xJnRpbWU9MzcyJmNoYW5uZWxfaWQ9MSZzdWJfY2hhbm5lbF9pZD0xJmFjY291bnRfdHlwZT0xJmRpc3BhdGNoU2VlZD0yMjdmYTQ3ZGE4Y2U3ZGNh".decodeBase64String()
    /* ktlint-enable max-line-length */

    @kotlinx.serialization.Serializable
    data class Data(
        val host: String = "0.0.0.0",
        val port: Int = 443,
        // TODO: Extract to multiple setting
        val gateServerIp: String = "localhost",
        val gateServerPort: Int = 22101,
        val servers: ArrayList<Server> = arrayListOf(Server()),
        val forwardCommonRequest: Boolean = true,
        // If false, dispatch server will use default config hardcoded in Sorapointa
        val forwardQueryCurrRegion: Boolean = true,
        val queryCurrRegionHardcode: String = QUERY_CURR_HARDCODE_DEFAULT,
        val password: Argon2PasswordSetting = Argon2PasswordSetting(),
        val useSSL: Boolean = true,
        var certification: Certification = Certification()
    )

    @kotlinx.serialization.Serializable
    data class Argon2PasswordSetting(
        val hashPepper: String = randomByteArray(256).hex,
        val saltByteLength: Int = 64,
        val memory: Int = 12,
        val iterations: Int = 20,
        val byteLength: Int = 32,
        val parallelism: Int = 2,
        val argon2Type: Argon2 = Argon2.ID,
        val argon2Version: Int = 19
    )

    @kotlinx.serialization.Serializable
    data class Certification(
        val keyStoreFilePath: String = File(configDirectory, KeyProvider.DEFAULT_CERT_NAME).absPath,
        val keyStore: String = "JKS",
        val keyAlias: String = KeyProvider.DEFAULT_ALIAS,
        val keyStorePassword: String = KeyProvider.DEFAULT_KEY_STORE_PASSWORD,
        val privateKeyPassword: String = KeyProvider.DEFAULT_CERT_PASSWORD
    )

    @kotlinx.serialization.Serializable
    data class Server(
        val serverName: String = "sorapointa_01",
        val title: String = "Sorapointa",
        val serverType: String = "DEV_PUBLIC",
        val dispatchDomain: String = "localhost",
    )
}
