package org.sorapointa.command.defaults.general

import moe.sdl.yac.core.UsageError
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.options.convert
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.int
import org.sorapointa.CoreBundle
import org.sorapointa.command.Command
import org.sorapointa.command.CommandManager
import org.sorapointa.command.CommandSender

class Help(private val sender: CommandSender) : Command(sender, Help) {
    companion object : Entry(
        name = "help",
        helpKey = "sora.cmd.help.desc",
        alias = listOf("?")
    )

    private val pageNum by argument(
        name = CoreBundle.message("sora.cmd.help.arg.page.num.name", locale = sender.locale),
        help = CoreBundle.message("sora.cmd.help.arg.page.num.desc", locale = sender.locale)
    ).int().default(1)

    private val pageSize by option(
        "--page-size", "-s",
        help = CoreBundle.message("sora.cmd.help.opt.page.size.desc", locale = sender.locale)
    ).int().convert {
        it.coerceIn(1..50)
    }.default(10)

    override suspend fun run() {
        val cmdList = CommandManager.commandEntries
        // Take out the page items from command list.
        val pageItems = cmdList.chunked(pageSize)
        // Throw exception when the page number exceed.
        if (pageNum !in 1..pageItems.size) {
            throw UsageError(CoreBundle.message("sora.cmd.help.arg.page.num.exceed", locale = sender.locale))
        }

        // Build the message and send to the sender
        sender.sendMessage(
            buildString {
                appendLine(CoreBundle.message("sora.cmd.help.msg.page", pageNum, pageItems.size))
                val entries = pageItems[pageNum - 1]
                val maxLen = entries.map { it.name }.maxOf { it.length }
                entries.forEach {
                    append(it.name.padEnd(maxLen, ' '))
                    append(" >> ")
                    if (it.helpKey.isNotBlank()) {
                        append(CoreBundle.message(it.helpKey))
                    } else append(CoreBundle.message("sora.cmd.help.msg.empty.desc", locale = sender.locale))
                    appendLine()
                }
                append(CoreBundle.message("sora.cmd.help.msg.more"))
            }
        )
    }
}
