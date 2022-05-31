package org.sorapointa.dispatch

import com.password4j.types.Argon2
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.data.provider.DatabaseConfig
import org.sorapointa.data.provider.DatabaseManager
import org.sorapointa.dispatch.data.AccountTable
import org.sorapointa.dispatch.data.ClientCustomConfig
import org.sorapointa.dispatch.data.DispatchKeyDataTable
import org.sorapointa.dispatch.data.RegionListClientCustomConfig
import org.sorapointa.dispatch.plugins.*
import org.sorapointa.dispatch.plugins.configureHTTP
import org.sorapointa.dispatch.plugins.configureMonitoring
import org.sorapointa.dispatch.plugins.configureRouting
import org.sorapointa.dispatch.plugins.configureSerialization
import org.sorapointa.dispatch.utils.KeyProvider
import org.sorapointa.utils.*
import org.sorapointa.utils.randomByteArray
import org.sorapointa.utils.encoding.hex
import java.io.File
import kotlin.text.toCharArray
import kotlin.time.Duration

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
        DatabaseManager.loadTables(AccountTable, DispatchKeyDataTable)
        val environment = getEnvironment()
        environment.setupApplication()
        embeddedServer(Netty, environment = environment).start(wait = isIndependent)
    }
}

/* ktlint-disable max-line-length */
private val QUERY_CURR_HARDCODE_DEFAULT = "aHR0cHM6Ly9jbmdmZGlzcGF0Y2gueXVhbnNoZW4uY29tL3F1ZXJ5X2N1cl9yZWdpb24/dmVyc2lvbj1DTlJFTFdpbjIuNy4wJmxhbmc9MiZwbGF0Zm9ybT0zJmJpbmFyeT0xJnRpbWU9ODE4JmNoYW5uZWxfaWQ9MSZzdWJfY2hhbm5lbF9pZD0xJmFjY291bnRfdHlwZT0xJmRpc3BhdGNoU2VlZD02N2QzZWUzOWZkYWFiMDlm".decodeBase64String()
private val QUERY_CURR_RSA_KEY = "PFJTQUtleVZhbHVlPjxNb2R1bHVzPnovZnlmb3psRElEV0c5ZTNMYjI5KzdqM2M2Nnd2VUpCYUJXUDEwckI5SFRFNnByamZjR01xQzlpbXI2ekFkRDlxK0dyMWo3ZWd2cWdpM0RhK1ZCQU1GSDkyLzV3RDVQc0Q3ZFg4WjJmNG82NVZrMm5WT1k4RGw3NVovdVJoZzBFdXduZnJ2ZWQ2OXo5TEc2dXRtbHl2NllVUEFmbFhoL0pGdzdEcTZjNEVHZVIrS2VqRlR3bVZoRWR6UEdIalhoRm1zVnQ5SGRYUllTZjROeEhQek93ajh0aVNhT1FBMGpDNEU0bU03cnZHU0g1R1g2aG1hKzdwSm5sLzUrckVWTTBtU1F2bTBtMVhlZm11RnkwNDBiRVovNk83WmVuT0dCc3Z2d3VHM1RUNEZORE56VzhEdzlFeEgxbDZOb1JHYVZrRGR0cmwvbkZ1NSthMDlQbS9FMEVsdz09PC9Nb2R1bHVzPjxFeHBvbmVudD5BUUFCPC9FeHBvbmVudD48UD45aGRVUnhlNkRuT3FTcGU2bmgyblZMVG14clBYTlkrRlNGQ2I0S3R1R0I1T3FtT2VBa2tRSHYyb3lzYWJLU0xRLzl3YTF0TnlzZC96Nkx1QU9VZ1piUTR4dmorT2ZoL2tBSlVQVFNMSytRZElZK2ZRQ0tZeWcwNHh1UWFpM3RLUktlZHpERmQxckRBUEpPN1oyaDllNEd2dmI0WmlxQkVBYm5ZaTREUUxTbEU9PC9QPjxRPjJGZW45VEpiK0cwSGJ0K3NweUgrdE1wQXFiWGFRRlhiUUNTUkZSQlNKdUtKREphNTVZcXo3bHRWcGJsSG1nTWlGYkdwKzBtMmNRVlpTOVpwTWVrZXdIOXVtTkxjSW5wYVNlbzF1bHJkQWhKeWxYVzdEeFg0UzNQOHJiOSsyUEpuTVdpZXB6NG01M25mcmpFVjBpVTZ4R1AyQm1jcnpkWnk2TG9RWEVCNnZtYz08L1E+PERQPm5OUFBOS010UWVwNk9xRXBIM3ljVjRJVms4bW1PNDdrREdxNmU5b2tCaURDVnhtMjU1UHlQeDIrQk1PK3U5OWhPN3prS2NXRTBWQjhXdk9xeWxabFJiZUhBY3YxSGZGcTF1Z25ZU3ZzRi9tSks0bmViTFNsZWtKSnM3VkQ5Q1pTdGxhMlhjWWF5b215RFFKZU9RQkc4VlEzdVdYMTEwOUdiQjdES1FoaHJaRT08L0RQPjxEUT5jbUt1V0ZOZkUxTzZXV0lFTEg0cDZMY0RSM2Z5UkkvZ2srS0JueXg0OHp4VmtBVmxscnNtZFlGdklHZDlOeTR1NkY5K2EzSEc5NjBIVUxTMS9BQ3hGTUNMM2x1bXJzZ1lVdnAxbSttTTd4cUg0UVJWZWgxNG9aUmE1aGJZMzZZUzc2bk1NTXNJME55OGFxSmpVakFEQ1hGODFGZmFia1BUajc5SkJTM0dlRU09PC9EUT48SW52ZXJzZVE+RjVoU0U5Ty9VS0pCNHlhMXMvMUdxQkZHNnpaRndmL0x5b2dPR1Jrc3pMWmQ0MUQwSFY2MVgzdEtIM2lvcWdrTE9IK0V0SFdCSXdyKy96aUlvMWVTOXVKby8yZFVPS3Z2a3VUcExDaXp6d0hkNEYrQUdHMFhJRDB1SzFDcGRhQTVQM21EZEFqV0F2dzJGZmJBTCt1WlYvRzkrUjJJYjF5RWxXTGNNRUx2L21JPTwvSW52ZXJzZVE+PEQ+clI5ZXduSlBpaVVHRjQ5dmNhaHVLc3BEVkEyc0d5QzRpZ2pKQVJPK2VkMXF2MUhJNXJya2VHMVp6Qy9MbkV0NW9FZndZQjFkNWZMMUNwOGI2a2NmNkJtWkZqV3MyNHJzQy9rNFFHNVMxcXF4Sm1MbVZRcUVIQUo3NUUvTFNLZzFzKzM0UXhMbVo1NURNMlhBRXlHYzRHVkVtdVNIejk3dDZ6L2pLMVc4bWduY3lSSGlOR0s3OVYwL2pPWFhaQ2tLMklLZ3VaRVltSXZ5NHpYQ3lZYWtsYmZLZCt3blNjZFR4aHhZeWltK0RHYVFEWlRVWUhrN1ZxUmxYMHREeVM4Mm9pTlRjajBpYis4Vm1ZRllXeXZmc0VNYWtodWlwbWVMNlJMMFNOY3lvcUwrUWJBQlRmaG43ZytacVo5VjZQUXFjMDM0LzdEdGQxYVJ4L2pMZk5Qc2dRPT08L0Q+PC9SU0FLZXlWYWx1ZT4=".decodeBase64String()
/* ktlint-enable max-line-length */

object DispatchConfig : DataFilePersist<DispatchConfig.Data>(
    File(configDirectory, "dispatchConfig.json"), Data()
) {

    @Serializable
    data class Data(
        val host: String = "0.0.0.0",
        val port: Int = 443,
        val gateServerIp: String = "localhost",
        val gateServerPort: Int = 22101,
        val servers: ArrayList<Server> = arrayListOf(Server()),
        val useSSL: Boolean = true,
        var certification: Certification = Certification(),
        val requestSetting: RequestSetting = RequestSetting(),
        val accountSetting: AccountSetting = AccountSetting(),
        val regionListClientCustomConfig: RegionListClientCustomConfig = RegionListClientCustomConfig(
            sdkEnvironment = 2u,
            showException = false,
            loadPatch = false,
            regionConfig = "",
            regionDispatchType = 0u,
            videoKey = 5578228838233776,
            downloadMode = 0u
        ),
        val clientCustomConfig: ClientCustomConfig = ClientCustomConfig(
            perfReportEnable = false,
            photographShareTopics = 753u,
            gachaShareTopics = 372u,
            photographSharePlatform = 13u,
            gachaSharePlatform = 13u,
            homeItemFilter = 20u,
            reportNetDelayConfig = ClientCustomConfig.ReportNetDelayConfigData(
                openGateServer = true
            )
        )
    )

    @Serializable
    data class RequestSetting(
        val forwardCommonRequest: Boolean = true,
        // If false, dispatch server will use default config hardcoded in Sorapointa
        val forwardQueryCurRegion: Boolean = true,
        val usingCurRegionUrlHardcode: Boolean = false,
        val curRegionContainsCustomClientConfig: Boolean = true,
        val queryCurRegionHardcode: String = QUERY_CURR_HARDCODE_DEFAULT,
        @SerialName("v2.8CurRegionForwardFormat")
        val v28: Boolean = false,
        @SerialName("v2.8RSAKey")
        val rsaKey: String = QUERY_CURR_RSA_KEY
    )

    @Serializable
    data class AccountSetting(
        @SerialName("comboExpiredExpiredTime")
        private val _comboTokenExpiredTime: String = "3d",
        @SerialName("dispatchTokenExpiredTime")
        private val _dispatchTokenExpiredTime: String = "3d",
        val password: Argon2PasswordSetting = Argon2PasswordSetting()
    ) {
        val comboTokenExpiredTime: Duration
            get() = Duration.parse(_comboTokenExpiredTime)

        val dispatchTokenExpiredTime: Duration
            get() = Duration.parse(_dispatchTokenExpiredTime)
    }

    @Serializable
    data class Argon2PasswordSetting(
        val usePepper: Boolean = false,
        val hashPepper: String = randomByteArray(256).hex,
        val saltByteLength: Int = 64,
        val memory: Int = 12,
        val iterations: Int = 20,
        val byteLength: Int = 32,
        val parallelism: Int = 2,
        val argon2Type: Argon2 = Argon2.ID,
        val argon2Version: Int = 19
    )

    @Serializable
    data class Certification(
        val keyStoreFilePath: String = File(configDirectory, KeyProvider.DEFAULT_CERT_NAME).absPath,
        val keyStore: String = "JKS",
        val keyAlias: String = KeyProvider.DEFAULT_ALIAS,
        val keyStorePassword: String = KeyProvider.DEFAULT_KEY_STORE_PASSWORD,
        val privateKeyPassword: String = KeyProvider.DEFAULT_CERT_PASSWORD
    )

    @Serializable
    data class Server(
        val serverName: String = "sorapointa_01",
        val title: String = "Sorapointa",
        val serverType: String = "DEV_PUBLIC",
        val dispatchDomain: String = "localhost"
    )
}
