package org.sorapointa.command.defaults.general

import moe.sdl.yac.core.UsageError
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.options.convert
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.int
import org.sorapointa.command.Command
import org.sorapointa.command.CommandManager
import org.sorapointa.command.CommandSender
import org.sorapointa.utils.i18n

class Help(private val sender: CommandSender) : Command(sender, Help) {
    companion object : Entry(
        name = "help",
        help = "sora.cmd.help.desc",
        alias = listOf("?")
    )

    private val pageNum by argument(
        name = "sora.cmd.help.arg.pagenum.name".i18n(locale = sender),
        help = "sora.cmd.help.arg.pagenum.desc".i18n(locale = sender)
    ).int().default(1)

    private val pageSize by option(
        "--page-size", "-s",
        help = "sora.cmd.help.opt.pagesize.desc".i18n(locale = sender),
    ).int().convert {
        it.coerceIn(1..50)
    }.default(10)

    override suspend fun run() {
        val cmdList = CommandManager.commandEntries
        // Take out the page items from command list.
        val pageItems = cmdList.chunked(pageSize)
        // Throw exception when the page number exceed.
        if (pageNum !in 1..pageItems.size) {
            throw UsageError("sora.cmd.help.arg.pagenum.exceed".i18n(locale = sender))
        }

        // Build the message and send to the sender
        sender.sendMessage(
            buildString {
                append("sora.cmd.help.msg.page".i18n(pageNum, pageItems.size))
                val entries = pageItems[pageNum - 1]
                val maxLen = entries.map { it.name }.maxOf { it.length }
                entries.forEach {
                    append(it.name.padEnd(maxLen, ' '))
                    append(" >> ")
                    if (it.help.isNotBlank()) {
                        append(it.help.i18n())
                    } else append("sora.cmd.help.msg.emptydesc".i18n(locale = sender))
                    appendLine()
                }
                append("sora.cmd.help.msg.more".i18n())
            }
        )
    }
}
