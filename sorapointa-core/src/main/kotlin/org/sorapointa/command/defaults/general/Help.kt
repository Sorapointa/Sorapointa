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
import org.sorapointa.command.localed

class Help(private val sender: CommandSender) : Command(sender, Help) {
    companion object : Entry(
        name = "help",
        helpKey = "sora.cmd.help.desc",
        alias = listOf("?"),
    )

    private val pageNum by argument(
        name = sender.localed("sora.cmd.help.arg.page.num.name"),
        help = sender.localed("sora.cmd.help.arg.page.num.desc"),
    ).int().default(1)

    private val pageSize by option(
        "--page-size",
        "-s",
        help = sender.localed("sora.cmd.help.opt.page.size.desc"),
    ).int().convert {
        it.coerceIn(1..50)
    }.default(10)

    override suspend fun run() {
        val cmdList = CommandManager.commandEntries
        // Take out the page items from command list.
        val pageItems = cmdList.chunked(pageSize)
        // Throw exception when the page number exceed.
        if (pageNum !in 1..pageItems.size) {
            throw UsageError(sender.localed("sora.cmd.help.arg.page.num.exceed"))
        }

        // Build the message and send to the sender
        sender.sendMessage(
            buildString {
                appendLine(sender.localed("sora.cmd.help.msg.page", pageNum, pageItems.size))
                val entries = pageItems[pageNum - 1]
                val maxLen = entries.map { it.name }.maxOf { it.length }
                entries.forEach {
                    append(it.name.padEnd(maxLen, ' '))
                    append(" >> ")
                    if (it.helpKey.isNotBlank()) {
                        append(sender.localed(it.helpKey))
                    } else {
                        append(sender.localed("sora.cmd.help.msg.empty.desc"))
                    }
                    appendLine()
                }
                append(sender.localed("sora.cmd.help.msg.more"))
            },
        )
    }
}
