package org.sorapointa.game

import mu.KotlinLogging
import org.sorapointa.Sorapointa
import org.sorapointa.dispatch.data.Account
import org.sorapointa.server.network.NetworkHandler
import org.sorapointa.server.network.NetworkHandlerStateInterface
import org.sorapointa.utils.ModuleScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

class Player internal constructor(
    val account: Account,
    internal val networkHandler: NetworkHandler,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) {

    inline val uid
        get() = account.userId.value

    internal val scope = ModuleScope(toString(), parentCoroutineContext)

    fun init() {
        logger.info { toString() + " has joined to the server" }
        networkHandler.networkStateController.observeStateChange { _, state ->
            if (state == NetworkHandlerStateInterface.State.CLOSED) {
                onConnectionClosed()
            }
        }
    }

    private fun onConnectionClosed() {
        logger.info { toString() + " has disconnected to the server" }
        Sorapointa.playerList.remove(this)
    }

    override fun toString(): String =
        "Player[id: ${account.userId}, host: ${networkHandler.getHost()}]"
}
