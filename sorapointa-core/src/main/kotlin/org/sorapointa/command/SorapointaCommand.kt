package org.sorapointa.command

import moe.sdl.yac.core.CliktCommand

abstract class SorapointaCommand(
    name: String,
    val alias: Array<String>,
    val type: CommandSenderType,
) : CliktCommand(name = name) {
    var sender: CommandSender? = null
}
