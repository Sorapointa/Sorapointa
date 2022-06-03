package org.sorapointa

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Yaml
import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.utils.configDirectory
import java.io.File

object SorapointaConfig : DataFilePersist<SorapointaConfig.Data>(
    File(configDirectory, "sorapointaConfig.yaml"), Data(), format = Yaml
) {

    @Serializable
    data class Data(
        val networkSetting: NetworkSetting = NetworkSetting()
    )

    @Serializable
    data class NetworkSetting(
        val bindPort: Int = 22101,
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
