package org.sorapointa.server

import io.jpower.kcp.netty.ChannelOptionHelper
import io.jpower.kcp.netty.UkcpChannelOption
import io.jpower.kcp.netty.UkcpServerChannel
import io.netty.bootstrap.UkcpServerBootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.nio.NioEventLoopGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.sorapointa.SorapointaConfig
import org.sorapointa.server.network.ConnectionInitializer
import org.sorapointa.utils.ModuleScope
import org.sorapointa.utils.awaitKt
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

object ServerNetwork {

    private var scope = ModuleScope("ServerNetwork")

    private lateinit var workerGroup: NioEventLoopGroup

    private lateinit var serverBootstrap: UkcpServerBootstrap

    internal fun boot(parentContext: CoroutineContext = EmptyCoroutineContext): Job {
        scope = ModuleScope("ServerNetwork", parentContext)
        val networkSetting = SorapointaConfig.data.networkSetting
        val port = networkSetting.bindPort
        logger.info { "Starting Sorapointa Server, binding server port on $port" }
        val job = scope.launch {
            workerGroup = NioEventLoopGroup()
            serverBootstrap = UkcpServerBootstrap()
            val future = serverBootstrap
                .group(workerGroup)
                .channel(UkcpServerChannel::class.java)
                .childHandler(ConnectionInitializer(scope))
                .bind(port)

            val ukcpSetting = networkSetting.uKcpSetting

            ChannelOptionHelper
                .nodelay(
                    serverBootstrap,
                    ukcpSetting.noDelay,
                    ukcpSetting.interval,
                    ukcpSetting.fastResend,
                    ukcpSetting.nocwnd
                )
                .childOption(UkcpChannelOption.UKCP_MTU, ukcpSetting.MTU)

            future.runCatching {
                awaitKt()
                channel().pipeline().addLast(object : ChannelInboundHandlerAdapter() {
                    @Deprecated("Deprecated in Java")
                    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                        scope.launch {
                            future.channel().close()
                            workerGroup.shutdownGracefully()
                            throw cause
                        }
                    }
                })
            }.onFailure {
                workerGroup.shutdownGracefully()
            }
        }

        return job
    }
}
