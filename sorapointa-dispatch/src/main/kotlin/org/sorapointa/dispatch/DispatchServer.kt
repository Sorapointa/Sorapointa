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
        embeddedServer(Netty, environment = environment).start(wait = true)
    }
}

/* ktlint-disable max-line-length */
private val QUERY_CURR_HARDCODE_DEFAULT = "aHR0cHM6Ly9jbmdmZGlzcGF0Y2gueXVhbnNoZW4uY29tL3F1ZXJ5X2N1cl9yZWdpb24/dmVyc2lvbj1DTlJFTFdpbjIuNy4wJmxhbmc9MiZwbGF0Zm9ybT0zJmJpbmFyeT0xJnRpbWU9ODE4JmNoYW5uZWxfaWQ9MSZzdWJfY2hhbm5lbF9pZD0xJmFjY291bnRfdHlwZT0xJmRpc3BhdGNoU2VlZD02N2QzZWUzOWZkYWFiMDlm".decodeBase64String()
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
            codeSwitch = listOf(15u, 2410u, 2324u, 21u),
            coverSwitch = listOf(8u, 40u),
            perfReportEnable = false,
            homeDotPattern = true,
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
        val forwardQueryCurrRegion: Boolean = true,
        val usingCurrRegionUrlHardcode: Boolean = true,
        val queryCurrRegionHardcode: String = QUERY_CURR_HARDCODE_DEFAULT,
        @SerialName("v2.8responseFormat")
        val v28: Boolean = false
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
