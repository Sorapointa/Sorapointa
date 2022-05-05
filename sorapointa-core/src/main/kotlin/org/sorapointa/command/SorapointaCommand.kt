package org.sorapointa.command

import moe.sdl.yac.core.CliktCommand

abstract class SorapointaCommand(
    name: String,
    help: String = "",
    val alias: Array<String> = emptyArray(),
    val type: CommandSenderType = CommandSenderType.ADMIN,
) : CliktCommand(name = name, help = help) {
    var sender: CommandSender? = null
}
