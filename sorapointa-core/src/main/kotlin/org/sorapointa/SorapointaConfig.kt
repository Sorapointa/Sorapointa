package org.sorapointa

import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.utils.configDirectory
import java.io.File
import kotlin.time.Duration

object SorapointaConfig : DataFilePersist<SorapointaConfig.Data>(
    File(configDirectory, "sorapointaConfig.yaml"), Data(), format = Yaml
) {

    @Serializable
    data class Data(
        @Comment("Game server network setting")
        val networkSetting: NetworkSetting = NetworkSetting(),
        @Comment("Player inventory store limits")
        val inventoryLimits: InventoryLimits = InventoryLimits(),
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
        @Comment("Game server bind port")
        val bindPort: Int = 22101,
        @Comment("Auto disconnect session if client dosen't send `PingReq` in specified time")
        @SerialName("pingTimeout")
        private val _pingTimeout: String = "20s",
        @Comment(
            """
            Game server kcp setting, don't change those settings if you don't know about KCP
            See more: https://github.com/skywind3000/kcp
        """
        )
        val uKcpSetting: UKcpSetting = UKcpSetting()
    ) {
        val pingTimeout: Duration
            get() = Duration.parse(_pingTimeout)
    }

    @Serializable
    data class UKcpSetting(
        val noDelay: Boolean = true,
        val interval: Int = 20,
        val fastResend: Int = 2,
        val nocwnd: Boolean = true,
        val MTU: Int = 1200,
    )
}
