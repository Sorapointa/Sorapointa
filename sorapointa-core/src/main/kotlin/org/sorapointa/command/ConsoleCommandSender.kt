package org.sorapointa.command

import io.ktor.server.websocket.*
import kotlinx.coroutines.isActive
import org.sorapointa.console.MessageNotify
import org.sorapointa.console.WebConsolePacket
import java.util.*

open class ConsoleCommandSender(
    override val locale: Locale? = null,
) : CommandSender {
    override suspend fun sendMessage(msg: String): Unit = println(msg)
}

class RemoteCommandSender(
    private val session: DefaultWebSocketServerSession,
    override val locale: Locale?,
) : ConsoleCommandSender() {
    val isActive
        get() = session.isActive

    override suspend fun sendMessage(msg: String) {
        session.sendSerialized<WebConsolePacket>(MessageNotify(msg))
    }
}
