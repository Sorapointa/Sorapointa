package org.sorapointa

import org.sorapointa.command.CommandManager
import org.sorapointa.dispatch.DispatchServer
import org.sorapointa.game.Player
import org.sorapointa.server.ServerNetwork
import org.sorapointa.utils.ModuleScope
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object Sorapointa {

    private var scope = ModuleScope("Sorapointa")

    val playerList = ConcurrentLinkedDeque<Player>()

    internal suspend fun init(parentContext: CoroutineContext = EmptyCoroutineContext) {
        scope = ModuleScope("Sorapointa", parentContext)
        CommandManager.init(scope.coroutineContext)
        ServerNetwork.boot(scope.coroutineContext)
        DispatchServer.startDispatch()
    }
}
