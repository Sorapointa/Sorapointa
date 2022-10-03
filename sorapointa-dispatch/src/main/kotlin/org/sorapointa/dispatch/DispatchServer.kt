package org.sorapointa.dispatch

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
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
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
import org.sorapointa.dispatch.plugins.configureStatusPage
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
            log = LoggerFactory.getLogger("ktor.application")
            if (dispatchConfig.useSSL) {
                sslConnector(
                    keyStore = cert,
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

/* ktlint-disable max-line-length */
private val QUERY_CURR_HARDCODE_DEFAULT =
    "aHR0cHM6Ly9jbmdmZGlzcGF0Y2gueXVhbnNoZW4uY29tL3F1ZXJ5X2N1cl9yZWdpb24/dmVyc2lvbj1DTlJFTFdpbjIuNy4wJmxhbmc9MiZwbGF0Zm9ybT0zJmJpbmFyeT0xJnRpbWU9ODE4JmNoYW5uZWxfaWQ9MSZzdWJfY2hhbm5lbF9pZD0xJmFjY291bnRfdHlwZT0xJmRpc3BhdGNoU2VlZD02N2QzZWUzOWZkYWFiMDlm".decodeBase64String()
private val QUERY_CURR_RSA_KEY =
    "PFJTQUtleVZhbHVlPjxNb2R1bHVzPnovZnlmb3psRElEV0c5ZTNMYjI5KzdqM2M2Nnd2VUpCYUJXUDEwckI5SFRFNnByamZjR01xQzlpbXI2ekFkRDlxK0dyMWo3ZWd2cWdpM0RhK1ZCQU1GSDkyLzV3RDVQc0Q3ZFg4WjJmNG82NVZrMm5WT1k4RGw3NVovdVJoZzBFdXduZnJ2ZWQ2OXo5TEc2dXRtbHl2NllVUEFmbFhoL0pGdzdEcTZjNEVHZVIrS2VqRlR3bVZoRWR6UEdIalhoRm1zVnQ5SGRYUllTZjROeEhQek93ajh0aVNhT1FBMGpDNEU0bU03cnZHU0g1R1g2aG1hKzdwSm5sLzUrckVWTTBtU1F2bTBtMVhlZm11RnkwNDBiRVovNk83WmVuT0dCc3Z2d3VHM1RUNEZORE56VzhEdzlFeEgxbDZOb1JHYVZrRGR0cmwvbkZ1NSthMDlQbS9FMEVsdz09PC9Nb2R1bHVzPjxFeHBvbmVudD5BUUFCPC9FeHBvbmVudD48UD45aGRVUnhlNkRuT3FTcGU2bmgyblZMVG14clBYTlkrRlNGQ2I0S3R1R0I1T3FtT2VBa2tRSHYyb3lzYWJLU0xRLzl3YTF0TnlzZC96Nkx1QU9VZ1piUTR4dmorT2ZoL2tBSlVQVFNMSytRZElZK2ZRQ0tZeWcwNHh1UWFpM3RLUktlZHpERmQxckRBUEpPN1oyaDllNEd2dmI0WmlxQkVBYm5ZaTREUUxTbEU9PC9QPjxRPjJGZW45VEpiK0cwSGJ0K3NweUgrdE1wQXFiWGFRRlhiUUNTUkZSQlNKdUtKREphNTVZcXo3bHRWcGJsSG1nTWlGYkdwKzBtMmNRVlpTOVpwTWVrZXdIOXVtTkxjSW5wYVNlbzF1bHJkQWhKeWxYVzdEeFg0UzNQOHJiOSsyUEpuTVdpZXB6NG01M25mcmpFVjBpVTZ4R1AyQm1jcnpkWnk2TG9RWEVCNnZtYz08L1E+PERQPm5OUFBOS010UWVwNk9xRXBIM3ljVjRJVms4bW1PNDdrREdxNmU5b2tCaURDVnhtMjU1UHlQeDIrQk1PK3U5OWhPN3prS2NXRTBWQjhXdk9xeWxabFJiZUhBY3YxSGZGcTF1Z25ZU3ZzRi9tSks0bmViTFNsZWtKSnM3VkQ5Q1pTdGxhMlhjWWF5b215RFFKZU9RQkc4VlEzdVdYMTEwOUdiQjdES1FoaHJaRT08L0RQPjxEUT5jbUt1V0ZOZkUxTzZXV0lFTEg0cDZMY0RSM2Z5UkkvZ2srS0JueXg0OHp4VmtBVmxscnNtZFlGdklHZDlOeTR1NkY5K2EzSEc5NjBIVUxTMS9BQ3hGTUNMM2x1bXJzZ1lVdnAxbSttTTd4cUg0UVJWZWgxNG9aUmE1aGJZMzZZUzc2bk1NTXNJME55OGFxSmpVakFEQ1hGODFGZmFia1BUajc5SkJTM0dlRU09PC9EUT48SW52ZXJzZVE+RjVoU0U5Ty9VS0pCNHlhMXMvMUdxQkZHNnpaRndmL0x5b2dPR1Jrc3pMWmQ0MUQwSFY2MVgzdEtIM2lvcWdrTE9IK0V0SFdCSXdyKy96aUlvMWVTOXVKby8yZFVPS3Z2a3VUcExDaXp6d0hkNEYrQUdHMFhJRDB1SzFDcGRhQTVQM21EZEFqV0F2dzJGZmJBTCt1WlYvRzkrUjJJYjF5RWxXTGNNRUx2L21JPTwvSW52ZXJzZVE+PEQ+clI5ZXduSlBpaVVHRjQ5dmNhaHVLc3BEVkEyc0d5QzRpZ2pKQVJPK2VkMXF2MUhJNXJya2VHMVp6Qy9MbkV0NW9FZndZQjFkNWZMMUNwOGI2a2NmNkJtWkZqV3MyNHJzQy9rNFFHNVMxcXF4Sm1MbVZRcUVIQUo3NUUvTFNLZzFzKzM0UXhMbVo1NURNMlhBRXlHYzRHVkVtdVNIejk3dDZ6L2pLMVc4bWduY3lSSGlOR0s3OVYwL2pPWFhaQ2tLMklLZ3VaRVltSXZ5NHpYQ3lZYWtsYmZLZCt3blNjZFR4aHhZeWltK0RHYVFEWlRVWUhrN1ZxUmxYMHREeVM4Mm9pTlRjajBpYis4Vm1ZRllXeXZmc0VNYWtodWlwbWVMNlJMMFNOY3lvcUwrUWJBQlRmaG43ZytacVo5VjZQUXFjMDM0LzdEdGQxYVJ4L2pMZk5Qc2dRPT08L0Q+PC9SU0FLZXlWYWx1ZT4=".decodeBase64String()
private val QUERY_CURR_RSA_PUB_KEY =
    "PFJTQUtleVZhbHVlPjxNb2R1bHVzPnd0L1o5OG84Z2J3OTRsYTA3QjEvQXBWQ3VIV0hHSTdQZDhGUEYzUHZOWWYxb1RZd2dSY3pRQmZQcUhmWHl0dFJSUDQ0bXFHNHRmcnoyek84Z1hFTlJTeURYdHp1N2RRR2gzaHUxdDg3VHBQYmlZY1ErWkhLNTh2NmR5MWpvMzBUVEs2NHNSbmp4SmZXcktZRHhTQnhCekRiS0NsenFsWTBKLzRtVmpLRnhrN3FTMEh2b1l5ZGxSbmh2SlZPTWRqdC9TVjZ3eUhSWTY2RnZPdmRrNkJWTG9tM0swV0JITmNGRTZDaEEzR1FjUit4eVgxWjA1OEF2aUZyeDZLUzQ1bXFSdWpVQzV2Wlh1d2J2Z3JJQ2dFVmxmT1NjSEZuclRsRlg4eXNNNEMxYlNiOEljeTNWOFhTYjdMakNtWEJlQjdUVXBXMnZqaEtsemdaZVd3TnUxRGFFdz09PC9Nb2R1bHVzPjxFeHBvbmVudD5BUUFCPC9FeHBvbmVudD48L1JTQUtleVZhbHVlPg==".decodeBase64String()
/* ktlint-enable max-line-length */

@SorapointaInternal
object DispatchConfig : DataFilePersist<DispatchConfig.Data>(
    File(configDirectory, "dispatchConfig.yaml"), Data(), format = Yaml,
) {

    @Serializable
    data class Data(
        @Comment("Bind address of dispach server")
        val host: String = "0.0.0.0",
        @Comment("Bind port of dispatch server")
        val port: Int = 443,
        @Comment(
            """
            Game server public ip address
            After client access `/query_cur_region` routing, 
            client connection will be forwarded to this address
        """
        )
        val gateServerIp: String = "localhost",
        @Comment("Game server port")
        val gateServerPort: Int = 22101,
        @Comment("Server list of routing `/query_region_list`")
        val servers: ArrayList<Server> = arrayListOf(Server()),
        @Comment(
            """
            Use SSL encrypted connection
            If you use fiddler, mitmdump, or some similar tools to forward your connection,
            and decrypted the forwarding traffic, you could turn off this option.
        """
        )
        val useSSL: Boolean = true,
        @Comment("SSL certification setting")
        var certification: Certification = Certification(),
        @Comment("Request handling setting")
        val requestSetting: RequestSetting = RequestSetting(),
        @Comment("Account system setting")
        val accountSetting: AccountSetting = AccountSetting(),
        @Comment(
            """
            Client config that will be included in `query_region_list`
            Don't change it unless you know what you are doing.
        """
        )
        val regionListClientCustomConfig: RegionListClientCustomConfig = RegionListClientCustomConfig(
            sdkEnvironment = 2,
            showException = false,
            loadPatch = false,
            regionConfig = "",
            regionDispatchType = 0,
            videoKey = 5578228838233776,
            downloadMode = 0
        ),
        @Comment(
            """
            Client config that will be included in `query_cur_region`
            Don't change it unless you know what you are doing.
            """
        )
        val clientCustomConfig: ClientCustomConfig = ClientCustomConfig(
            perfReportEnable = false,
            photographShareTopics = 753,
            gachaShareTopics = 372,
            photographSharePlatform = 13,
            gachaSharePlatform = 13,
            homeItemFilter = 20,
            reportNetDelayConfig = ClientCustomConfig.ReportNetDelayConfigData(
                openGateServer = true
            )
        )
    )

    @Serializable
    data class RequestSetting(
        @Comment(
            """
            Forward the common request to original website
            For example, `/agreement/api/getAgreementInfos` will be forwared.
            If you turn off this option, dispatch server will use default config hardcoded in Sorapointa.
        """
        )
        val forwardCommonRequest: Boolean = true,
        @Comment(
            """
            Forward `query_cur_region` request to original website
            And sorapointa will auto replace important settings of the final request result,
            such as gate server address and custom config.
            If you turn off this option, sorapointa will only include those important settings metioned above.
        """
        )
        val forwardQueryCurrentRegion: Boolean = true,
        @Comment(
            """
           Forward `query_cur_region` to a hardcode url, rather than using client request arguments
           In general, this option should be used for accessing beta or other client version.
        """
        )
        val usingCurrentRegionUrlHardcode: Boolean = false,
        @Comment(
            """
            In some special cases, containing custom client config will cause client stuck in loading scene,
            turn off this option may solve this issue.
        """
        )
        val queryCurrentRegionHardcode: String = QUERY_CURR_HARDCODE_DEFAULT,
        val currentRegionContainsCustomClientConfig: Boolean = true,
        @Comment("Response `query_cur_region` in client 2.8 or higher version format")
        val v28CurrentRegionForwardFormat: Boolean = false,
        @Comment("`query_cur_region` RSA sign verify")
        val enableSignVerify: Boolean = true,
        @Comment(
            """
            If dispatch server try to forward 2.8 or higher version, 
            dispatch server will use this RSA public key to verify signature of data
        """
        )
        @SerialName("v2.8RSAPublicKey")
        val rsaPublicKey: String = QUERY_CURR_RSA_PUB_KEY,
        @Comment(
            """
            If dispatch server try to forward 2.8 or higher version, 
            dispatch server will use this RSA key to decrypt the forwarding result.
        """
        )
        @SerialName("v2.8RSAPrivateKey")
        val rsaPrivateKey: String = QUERY_CURR_RSA_KEY,
    )

    @Serializable
    data class AccountSetting(
        @Comment("Combo token will be used for login game server")
        @SerialName("comboExpiredExpiredTime")
        private val _comboTokenExpiredTime: String = "3d",
        @Comment("Dispatch token will be used for auto login dispatch server")
        @SerialName("dispatchTokenExpiredTime")
        private val _dispatchTokenExpiredTime: String = "3d",
        @Comment(
            """
            Password hash setting
            Don't change it unless you know what you are doing.
            
            More info: https://github.com/Password4j/password4j/wiki/Argon2
        """
        )
        val password: Argon2PasswordSetting = Argon2PasswordSetting()
    ) {
        val comboTokenExpiredTime: Duration
            get() = Duration.parse(_comboTokenExpiredTime)

        val dispatchTokenExpiredTime: Duration
            get() = Duration.parse(_dispatchTokenExpiredTime)
    }

    @Serializable
    data class Argon2PasswordSetting(
        @Comment(
            """
            Please decide to enable or disable this option at start and keep it, 
            or it will cause incompatible issue.
            """
        )
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
        @Comment("This title will be displayed on client screen")
        val title: String = "Sorapointa",
        @Comment("DEV_PUBLIC or DEV_PRIVATE, don't change it unless you know what you are doing")
        val serverType: String = "DEV_PUBLIC",
        @Comment(
            """
            According region dispatch server domain
            
            In general, it should be dispatch server public address
            Client will access `/query_region_list` before `/query_cur_region`,
            and client will access `/query_cur_region` for game server address information, 
            thus `/query_cur_region` should be on a separate server, 
            but Sorapointa dispatch server has done those two things together,
            so unless you want to setup multiple game or dispatch servers,
            just keep it as public address of dispatch server.
        """
        )
        val dispatchDomain: String = "localhost"
    )
}
