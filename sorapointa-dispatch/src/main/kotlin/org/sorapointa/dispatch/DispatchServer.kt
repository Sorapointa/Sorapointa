package org.sorapointa.dispatch

import com.charleskorn.kaml.YamlComment
import com.password4j.types.Argon2
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.data.provider.DatabaseConfig
import org.sorapointa.data.provider.DatabaseManager
import org.sorapointa.dispatch.data.AccountTable
import org.sorapointa.dispatch.data.ClientCustomConfig
import org.sorapointa.dispatch.data.DispatchKeyDataTable
import org.sorapointa.dispatch.data.RegionListClientCustomConfig
import org.sorapointa.dispatch.plugins.*
import org.sorapointa.dispatch.utils.KeyProvider
import org.sorapointa.utils.*
import org.sorapointa.utils.encoding.hex
import java.io.File
import kotlin.text.toCharArray
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

internal fun main(): Unit = runBlocking {
    DispatchServer.startDispatch(this, isIndependent = true).join()
}

object DispatchServer {

    internal val client by lazy {
        HttpClient(CIO) {
            install(Logging)
            install(ContentNegotiation) {
                json(prettyJson)
            }
        }
    }

    private suspend fun getEnvironment(): ApplicationEngineEnvironment {
        val cert = KeyProvider.getCertsFromConfigOrGenerate()
        return applicationEngineEnvironment {
            val dispatchConfig = DispatchConfig.data
            log = LoggerFactory.getLogger("DispatchServer")
            if (dispatchConfig.useSSL) {
                sslConnector(
                    keyStore = cert,
                    keyAlias = dispatchConfig.certification.keyAlias,
                    keyStorePassword = { dispatchConfig.certification.keyStorePassword.toCharArray() },
                    privateKeyPassword = { dispatchConfig.certification.privateKeyPassword.toCharArray() },
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
    }

    private fun ApplicationEngineEnvironment.setupApplication(): Application =
        application.apply {
            configureSerialization()
            configureStatusPage()
            configureMonitoring()
            configureHTTP()
            configureRouting()
        }

    @SorapointaInternal
    fun startDispatch(
        scope: CoroutineScope,
        isIndependent: Boolean = false,
        config: (Application) -> Unit = {},
    ): Job = scope.launch {
        if (isIndependent) {
            DispatchConfig.init()
            DispatchConfig.save()
            I18nConfig.init()
            I18nConfig.save()
            DatabaseConfig.init()
            DatabaseConfig.save()
            DatabaseManager.loadDatabase()
        }
        DatabaseManager.loadTables(AccountTable, DispatchKeyDataTable)
        val environment = getEnvironment()
        environment.setupApplication()
        environment.application.apply(config)
        val dispatchConfig = DispatchConfig.data
        val url = "http${if (dispatchConfig.useSSL) "s" else ""}://${dispatchConfig.host}:${dispatchConfig.port}"
        logger.info { "Starting Sorapointa dispatch server, responding at $url" }
        embeddedServer(Netty, environment = environment).start(wait = true)
    }
}

@SorapointaInternal
object DispatchConfig : DataFilePersist<DispatchConfig.Data>(
    File(configDirectory, "dispatchConfig.yaml"),
    Data(),
    Data.serializer(),
    lenientYaml,
) {

    @Serializable
    data class Data(
        @YamlComment("Bind address of dispach server")
        val host: String = "0.0.0.0",
        @YamlComment("Bind port of dispatch server")
        val port: Int = 443,
        @YamlComment(
            "Game server public ip address",
            "After client access `/query_cur_region` routing,",
            "client connection will be forwarded to this address",
        )
        val gateServerIp: String = "localhost",
        @YamlComment("Game server port")
        val gateServerPort: Int = 22101,
        @YamlComment("Server list of routing `/query_region_list`")
        val servers: ArrayList<Server> = arrayListOf(Server()),
        @YamlComment(
            "Use SSL encrypted connection",
            "If you use fiddler, mitmdump, or some similar tools to forward your connection,",
            "and decrypted the forwarding traffic, you could turn off this option.",
        )
        val useSSL: Boolean = true,
        @YamlComment("SSL certification setting")
        var certification: Certification = Certification(),
        @YamlComment("Request handling setting")
        val requestSetting: RequestSetting = RequestSetting(),
        @YamlComment("Account system setting")
        val accountSetting: AccountSetting = AccountSetting(),
        @YamlComment(
            "Client config that will be included in `query_region_list`",
            "Don't change it unless you know what you are doing.",
        )
        val regionListClientCustomConfig: RegionListClientCustomConfig = RegionListClientCustomConfig(
            sdkEnvironment = 2,
            showException = false,
            loadPatch = false,
            regionConfig = "",
            regionDispatchType = 0,
            videoKey = 5578228838233776,
            downloadMode = 0,
        ),
        @YamlComment(
            "Client config that will be included in `query_cur_region`",
            "Don't change it unless you know what you are doing.",
        )
        val clientCustomConfig: ClientCustomConfig = ClientCustomConfig(
            perfReportEnable = false,
            photographShareTopics = 753,
            gachaShareTopics = 372,
            photographSharePlatform = 13,
            gachaSharePlatform = 13,
            homeItemFilter = 20,
            reportNetDelayConfig = ClientCustomConfig.ReportNetDelayConfigData(
                openGateServer = true,
            ),
        ),
    )

    @Serializable
    data class RequestSetting(
        @YamlComment(
            "Forward the common request to original website",
            "For example, `/agreement/api/getAgreementInfos` will be forwarded.",
            "If you turn off this option, dispatch server will use default config hardcoded in Sorapointa.",
        )
        val forwardCommonRequest: Boolean = true,
        @YamlComment(
            "Forward `query_cur_region` request to original website",
            "And Sorapointa will auto replace important settings of the final request result,",
            "such as gate server address and custom config.",
            "If you turn off this option, sorapointa will only include those important settings metioned above.",
        )
        val forwardQueryCurrentRegion: Boolean = true,
        @YamlComment(
            "Forward `query_cur_region` to a hardcode url, rather than using client request arguments",
            "In general, this option should be used for accessing beta or other client version.",
        )
        val usingCurrentRegionUrlHardcode: Boolean = false,
        val queryCurrentRegionHardcode: String = "",
        @YamlComment(
            "In some special cases, containing custom client config will cause client stuck in loading scene,",
            "turn off this option may solve this issue.",
        )
        val currentRegionContainsCustomClientConfig: Boolean = true,
        @YamlComment("Response `query_cur_region` in the old 2.7 and lower version format")
        val oldCurrentRegionFormat: Boolean = false,
        @YamlComment("`query_cur_region` RSA sign verify")
        val enableSignVerify: Boolean = true,
        @YamlComment("Sign the changed `query_cur_region` response by following RSA private key")
        val enableSignature: Boolean = true,
        val queryCurrentRegionCacheFile: String = "cache/query_curr_region_http_rsp.bin",
    )

    @Serializable
    data class AccountSetting(
        @YamlComment("Combo token will be used for login game server")
        @SerialName("comboExpiredExpiredTime")
        private val _comboTokenExpiredTime: String = "3d",
        @YamlComment("Dispatch token will be used for auto login dispatch server")
        @SerialName("dispatchTokenExpiredTime")
        private val _dispatchTokenExpiredTime: String = "3d",
        @YamlComment(
            "Password hash setting",
            "Don't change it unless you know what you are doing.",
            "",
            "More info: https://github.com/Password4j/password4j/wiki/Argon2",
        )
        val password: Argon2PasswordSetting = Argon2PasswordSetting(),
    ) {
        val comboTokenExpiredTime: Duration
            get() = Duration.parse(_comboTokenExpiredTime)

        val dispatchTokenExpiredTime: Duration
            get() = Duration.parse(_dispatchTokenExpiredTime)
    }

    @Serializable
    data class Argon2PasswordSetting(
        @YamlComment(
            "Please decide to enable or disable this option at start and keep it,",
            "or it will cause incompatible issue.",
        )
        val usePepper: Boolean = false,
        val hashPepper: String = randomByteArray(256).hex,
        val saltByteLength: Int = 64,
        val memory: Int = 12,
        val iterations: Int = 20,
        val byteLength: Int = 32,
        val parallelism: Int = 2,
        val argon2Type: Argon2 = Argon2.ID,
        val argon2Version: Int = 19,
    )

    @Serializable
    data class Certification(
        val keyStoreFilePath: String = File(
            configDirectory,
            KeyProvider.DEFAULT_CERT_NAME + KeyProvider.defaultKeyStoreFileExtension,
        ).absPath,
        val keyStore: String = "JKS",
        val keyAlias: String = KeyProvider.DEFAULT_ALIAS,
        val keyStorePassword: String = KeyProvider.DEFAULT_KEY_STORE_PASSWORD,
        val privateKeyPassword: String = KeyProvider.DEFAULT_CERT_PASSWORD,
    )

    @Serializable
    data class Server(
        val serverName: String = "sorapointa_01",
        @YamlComment("This title will be displayed on client screen")
        val title: String = "Sorapointa",
        @YamlComment("DEV_PUBLIC or DEV_PRIVATE, don't change it unless you know what you are doing")
        val serverType: String = "DEV_PUBLIC",
        @YamlComment(
            "According region dispatch server domain",
            "",
            "In general, it should be dispatch server public address",
            "Client will access `/query_region_list` before `/query_cur_region`,",
            "and client will access `/query_cur_region` for game server address information,",
            "thus `/query_cur_region` should be on a separate server,",
            "but Sorapointa dispatch server has done those two things together,",
            "so unless you want to setup multiple game or dispatch servers,",
            "just keep it as public address of dispatch server.",
        )
        val dispatchDomain: String = "localhost",
    )
}
