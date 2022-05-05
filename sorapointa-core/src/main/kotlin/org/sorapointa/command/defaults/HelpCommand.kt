package org.sorapointa.command.defaults

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.types.int
import org.sorapointa.command.CommandManager
import org.sorapointa.command.SorapointaCommand

object HelpCommand : SorapointaCommand(
    name = "help", alias = arrayOf("h")
) {
    private const val PAGE_SIZE = 1

    private val pageNum by argument(
        help = "The page number of the help."
    ).int().default(1)

    override fun run() {
        val cmdList = CommandManager.cmdList
        val pageList = cmdList.subList(
            (pageNum - 1) * PAGE_SIZE, pageNum * PAGE_SIZE
        )

        sender?.sendMessage(buildString {
            append("===== Page $pageNum / ${cmdList.size / PAGE_SIZE} =====\n")
            pageList.forEach { append("${it.getFormattedUsage()}\n") }
            append("Use 'command --help' to get more detail.")
        })
    }
}
