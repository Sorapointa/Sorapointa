package org.sorapointa.game

import mu.KotlinLogging
import org.sorapointa.Sorapointa
import org.sorapointa.command.CommandSender
import org.sorapointa.dispatch.data.Account
import org.sorapointa.game.data.PlayerData
import org.sorapointa.server.network.NetworkHandler
import org.sorapointa.server.network.NetworkHandlerStateInterface
import org.sorapointa.utils.ModuleScope
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

class Player internal constructor(
    val account: Account,
    val data: PlayerData,
    internal val networkHandler: NetworkHandler,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : CommandSender {

    inline val uid
        get() = account.id.value

    internal val scope = ModuleScope(toString(), parentCoroutineContext)

    override var locale: Locale = data.locale

    fun init() {
        logger.info { toString() + " has joined to the server" }
        networkHandler.networkStateController.observeStateChange { _, state ->
            if (state == NetworkHandlerStateInterface.State.CLOSED) {
                onConnectionClosed()
            }
        }
    }

    override suspend fun sendMessage(msg: String) {
    }

    private fun onConnectionClosed() {
        logger.info { toString() + " has disconnected to the server" }
        Sorapointa.playerList.remove(this)
    }

    internal fun onLogin() {
    }

    override fun toString(): String =
        "Player[id: $uid, host: ${networkHandler.getHost()}]"
}
