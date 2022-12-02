package org.sorapointa

import com.charleskorn.kaml.YamlComment
import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.utils.configDirectory
import org.sorapointa.utils.lenientYaml
import java.io.File
import kotlin.time.Duration

object SorapointaConfig : DataFilePersist<SorapointaConfig.Data>(
    File(configDirectory, "sorapointaConfig.yaml"), Data(), Data.serializer(), lenientYaml
) {

    @Serializable
    data class Data(
        @YamlComment("Game server network setting")
        val networkSetting: NetworkSetting = NetworkSetting(),
        @YamlComment("Player inventory store limits")
        val inventoryLimits: InventoryLimits = InventoryLimits(),
        @YamlComment("Run sorapointa with dispatch server, sorapointa allow you to run them separately")
        val startWithDispatch: Boolean = true,
        @YamlComment("Use current region info for login rsp")
        val useCurrentRegionForLoginRsp: Boolean = true,
        val offsetHours: Int = 4,
        @SerialName("timeZone")
        private val _timeZone: String = TimeZone.currentSystemDefault().toString(),
    ) {

        val timeZone by lazy {
            TimeZone.of(_timeZone)
        }
    }

    @Serializable
    data class InventoryLimits(
        val weapon: Int = 2000,
        val reliquary: Int = 2000,
        val material: Int = 2000,
        val furniture: Int = 2000,
        val allWeight: Int = 30000
    )

    @Serializable
    data class NetworkSetting(
        @YamlComment("Game server bind port")
        val bindPort: Int = 22101,
        @YamlComment("Auto disconnect session if client dosen't send `PingReq` in specified time")
        @SerialName("pingTimeout")
        private val _pingTimeout: String = "20s",
        @YamlComment(
            "Game server kcp setting, don't change those settings if you don't know about KCP",
            "See more: https://github.com/skywind3000/kcp",
        )
        val uKcpSetting: UKcpSetting = UKcpSetting()
    ) {
        val pingTimeout: Duration
            get() = Duration.parse(_pingTimeout)
    }

    @Serializable
    data class UKcpSetting(
        val noDelay: Boolean = true,
        val interval: Int = 40,
        val fastResend: Int = 2,
        val noCongestionWindow: Boolean = true,
        val MTU: Int = 1400,
        val sendWindow: Int = 256,
        val receiveWindow: Int = 256,
        val timeoutMillis: Long = 30 * 1000, // KCP Timeout > Protocol Ping Timeout
        val ackNoDelay: Boolean = false
    )
}
