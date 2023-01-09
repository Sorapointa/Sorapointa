package org.sorapointa

import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sorapointa.command.CommandManager
import org.sorapointa.dispatch.DispatchServer
import org.sorapointa.dispatch.plugins.currentRegionRsp
import org.sorapointa.dispatch.plugins.getCurrentRegionHttpRsp
import org.sorapointa.game.Player
import org.sorapointa.game.impl
import org.sorapointa.server.ServerNetwork
import org.sorapointa.utils.ModuleScope
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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
        return if (SorapointaConfig.data.startWithDispatch) {
            DispatchServer.startDispatch(serverScope, config = config)
        } else {
            // If we don't start with dispatch server,
            // we will need some CurRegHttpRsp data for future processing.
            // And since we don't have original request from player client, or sth,
            // so we can only use the hardcode URL in
            scope.launch {
                currentRegionRsp.complete(getCurrentRegionHttpRsp())
            }
        }
    }

    suspend fun addPlayer(player: Player) {
        player.impl().init()
        playerList.add(player)
    }

    fun findPlayerById(id: Int) =
        playerList.firstOrNull { it.uid == id }
}
