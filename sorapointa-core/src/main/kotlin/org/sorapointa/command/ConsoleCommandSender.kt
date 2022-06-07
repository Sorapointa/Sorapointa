package org.sorapointa.command

import java.util.*

class ConsoleCommandSender(
    override val locale: Locale? = null
) : CommandSender {
    override suspend fun sendMessage(msg: String): Unit = println(msg)
}
