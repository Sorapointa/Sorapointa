package org.sorapointa.command

import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.core.context


abstract class SorapointaCommand(
    name: String,
    help: String = "",
    val alias: Array<String> = emptyArray(),
    val type: CommandSenderType = CommandSenderType.ADMIN,
) : CliktCommand(name = name, help = help) {
    init {
        context { localization = CommandLocalization }
    }
    var sender: CommandSender? = null
}
