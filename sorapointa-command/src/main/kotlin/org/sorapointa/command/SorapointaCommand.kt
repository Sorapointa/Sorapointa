package org.sorapointa.command

import moe.sdl.yac.core.CliktCommand

abstract class SorapointaCommand(
    name: String,
    val alias: Array<String>,
    val permission: String,
) : CliktCommand(name = name) {
    lateinit var sender: CommandSender
}
