package org.sorapointa.command

abstract class CommandSender(
    val type: CommandSenderType = CommandSenderType.SERVER
) {
    abstract fun sendMessage(msg: String)
}

enum class CommandSenderType {
    PLAYER,
    ADMIN,
    SERVER,
}
