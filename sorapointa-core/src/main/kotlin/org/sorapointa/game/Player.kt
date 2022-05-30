package org.sorapointa.game

import mu.KotlinLogging
import org.sorapointa.server.network.NetworkHandler
import org.sorapointa.server.network.NetworkHandlerStateInterface
import org.sorapointa.utils.ModuleScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

internal class Player(
    private val networkHandler: NetworkHandler,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) {

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


