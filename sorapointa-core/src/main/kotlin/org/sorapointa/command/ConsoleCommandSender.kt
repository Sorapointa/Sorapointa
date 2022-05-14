package org.sorapointa.command

class ConsoleCommandSender : CommandSender(
    locale = null
) {
    override fun sendMessage(msg: String): Unit = println(msg)
}
