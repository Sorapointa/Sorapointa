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

    private var scope = ModuleScope(logger, "ServerNetwork")

    private lateinit var workerGroup: NioEventLoopGroup

    private lateinit var serverBootstrap: UkcpServerBootstrap

    internal fun boot(parentContext: CoroutineContext = EmptyCoroutineContext): Job {
        logger.info { "Starting Sorapointa Server..." }
        scope = ModuleScope(logger, "ServerNetwork", parentContext)
        val port = SorapointaConfig.data.networkSetting.bindPort
        val job = scope.launch {
            workerGroup = NioEventLoopGroup()
            serverBootstrap = UkcpServerBootstrap()
            val future = serverBootstrap
                .group(workerGroup)
                .channel(UkcpServerChannel::class.java)
                .childHandler(ConnectionInitializer(scope))
                .bind(port)

            val ukcpSetting = SorapointaConfig.data.networkSetting.uKcpSetting

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
                logger.info { "Bind server port on $port" }
            }.onFailure {
                workerGroup.shutdownGracefully()
            }

        }

        return job
    }
}
