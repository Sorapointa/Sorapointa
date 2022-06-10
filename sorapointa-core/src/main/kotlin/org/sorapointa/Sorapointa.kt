package org.sorapointa

import org.sorapointa.command.CommandManager
import org.sorapointa.dispatch.DispatchServer
import org.sorapointa.game.Player
import org.sorapointa.server.ServerNetwork
import org.sorapointa.utils.ModuleScope
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import io.ktor.server.application.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

object Sorapointa {

    private var scope = ModuleScope("Sorapointa")

    val playerList = ConcurrentLinkedDeque<Player>()

    internal fun init(
        serverScope: CoroutineScope,
        parentContext: CoroutineContext = EmptyCoroutineContext,
        config: (Application) -> Unit = {},
    ): Job {
        scope = ModuleScope("Sorapointa", parentContext)
        CommandManager.init(scope.coroutineContext)
        ServerNetwork.boot(scope.coroutineContext)
        return DispatchServer.startDispatch(serverScope, config = config)
    }
}
