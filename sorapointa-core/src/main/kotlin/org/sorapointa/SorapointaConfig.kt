package org.sorapointa

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.utils.configDirectory
import java.io.File

object SorapointaConfig : DataFilePersist<SorapointaConfig.Data>(
    File(configDirectory, "sorapointaConfig.yaml"), Data(), format = Yaml
) {

    @Serializable
    data class Data(
        @Comment("Game server network setting")
        val networkSetting: NetworkSetting = NetworkSetting()
    )

    @Serializable
    data class NetworkSetting(
        @Comment("Game server bind port")
        val bindPort: Int = 22101,
        @Comment("""
            Game server kcp setting, don't change those settings if you don't know about KCP
            See more: https://github.com/skywind3000/kcp
        """)
        val uKcpSetting: UKcpSetting = UKcpSetting()
    )

    @Serializable
    data class UKcpSetting(
        val noDelay: Boolean = true,
        val interval: Int = 20,
        val fastResend: Int = 2,
        val nocwnd: Boolean = true,
        val MTU: Int = 1200,
    )
}
