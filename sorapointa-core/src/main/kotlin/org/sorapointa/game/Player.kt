package org.sorapointa.game

import mu.KotlinLogging
import org.sorapointa.dispatch.data.Account
import org.sorapointa.server.network.NetworkHandler
import org.sorapointa.server.network.NetworkHandlerStateInterface
import org.sorapointa.utils.ModuleScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

class Player internal constructor(
    internal val networkHandler: NetworkHandler,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) {

    lateinit var account: Account

    private val scope = ModuleScope(logger, "Player[${networkHandler.getHost()}]", parentCoroutineContext)

    fun init() {
        networkHandler.networkStateController.observeStateChange { _, state ->
            if (state == NetworkHandlerStateInterface.State.CLOSED) {
                onConnectionClosed()
            }
        }
    }

    private fun onConnectionClosed() {
    }
}
