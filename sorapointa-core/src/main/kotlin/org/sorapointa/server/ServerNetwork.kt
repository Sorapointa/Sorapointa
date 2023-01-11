package org.sorapointa.server

import kcp.highway.ChannelConfig
import kcp.highway.KcpServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.sorapointa.SorapointaConfig
import org.sorapointa.server.network.ConnectionListener
import org.sorapointa.utils.ModuleScope
import java.net.InetSocketAddress
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

object ServerNetwork {

    private var scope = ModuleScope("ServerNetwork")

    internal fun boot(parentContext: CoroutineContext = EmptyCoroutineContext): Job {
        scope = ModuleScope("ServerNetwork", parentContext)
        val networkSetting = SorapointaConfig.data.networkSetting
        val port = networkSetting.bindPort
        logger.info { "Starting Sorapointa Server, binding server port on $port" }
        val job = scope.launch {
            val ukcpSetting = networkSetting.uKcpSetting
            KcpServer().init(
                ConnectionListener(scope),
                ChannelConfig().apply {
                    nodelay(
                        ukcpSetting.noDelay,
                        ukcpSetting.interval,
                        ukcpSetting.fastResend,
                        ukcpSetting.noCongestionWindow,
                    )
                    mtu = ukcpSetting.MTU
                    sndwnd = ukcpSetting.sendWindow
                    rcvwnd = ukcpSetting.receiveWindow
                    timeoutMillis = ukcpSetting.timeoutMillis
                    isUseConvChannel = true
                    isAckNoDelay = ukcpSetting.ackNoDelay
                },
                InetSocketAddress(port),
            )
        }

        return job
    }
}
