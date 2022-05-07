package org.sorapointa.command.defaults

import moe.sdl.yac.core.UsageError
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.types.int
import org.sorapointa.command.CommandManager
import org.sorapointa.command.SorapointaCommand
import org.sorapointa.utils.i18n

/** A simple impl of help command. */
object HelpCommand : SorapointaCommand(
    name = "help", alias = arrayOf("h")
) {
    // Just a const value for page size.
    private const val PAGE_SIZE = 1

    // An int argument for the page number which specify the name and help.
    private val pageNum by argument(
        name = "sora.cmd.help.arg.pagenum.name".i18n(),
        help = "sora.cmd.help.arg.pagenum.desc".i18n()
    ).int().default(1)

    override fun run() {
        val cmdList = CommandManager.cmdList
        val totalPages = cmdList.size / PAGE_SIZE
        // Throw exception when the page number exceed.
        if (pageNum > totalPages) throw UsageError(
            "sora.cmd.help.arg.pagenum.exceed".i18n()
        )
        // Take out the page items from command list.
        val pageItems = cmdList.subList(
            (pageNum - 1) * PAGE_SIZE, pageNum * PAGE_SIZE
        )
        // Build the message and send to the sender
        sender?.sendMessage(buildString {
            append("sora.cmd.help.msg.page".i18n(pageNum, totalPages))
            pageItems.forEach { append("${it.getFormattedUsage()}\n") }
            append("sora.cmd.help.msg.more".i18n())
        })
    }
}
