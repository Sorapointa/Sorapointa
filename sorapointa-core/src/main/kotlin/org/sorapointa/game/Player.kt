package org.sorapointa.game

import org.sorapointa.dispatch.data.Account
import org.sorapointa.server.network.NetworkHandler
import org.sorapointa.server.network.NetworkHandlerStateInterface
import org.sorapointa.utils.ModuleScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Player internal constructor(
    internal val networkHandler: NetworkHandler,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) {

    lateinit var account: Account

    inline val uid
        get() = account.userId.value

    internal val scope = ModuleScope("Player[${networkHandler.getHost()}]", parentCoroutineContext)

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
