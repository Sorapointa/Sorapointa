package org.sorapointa.command.defaults

import moe.sdl.yac.core.UsageError
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.types.int
import org.sorapointa.command.CommandManager
import org.sorapointa.command.SorapointaCommand
import org.sorapointa.utils.i18n

object HelpCommand : SorapointaCommand(
    name = "help", alias = arrayOf("h")
) {
    private const val PAGE_SIZE = 1

    private val pageNum by argument(
        name = "sora.cmd.help.arg.pagenum.name".i18n(),
        help = "sora.cmd.help.arg.pagenum.desc".i18n()
    ).int().default(1)

    override fun run() {
        val cmdList = CommandManager.cmdList
        val totalPages = cmdList.size / PAGE_SIZE
        if (pageNum > totalPages) throw UsageError(
            "sora.cmd.help.arg.pagenum.exceed".i18n()
        )
        val pageList = cmdList.subList(
            (pageNum - 1) * PAGE_SIZE, pageNum * PAGE_SIZE
        )
        sender?.sendMessage(buildString {
            append("sora.cmd.help.msg.page".i18n(pageNum, totalPages))
            pageList.forEach { append("${it.getFormattedUsage()}\n") }
            append("sora.cmd.help.msg.more".i18n())
        })
    }
}
