package org.sorapointa.command

import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.core.context

abstract class Command(
    entry: Entry,
) : CliktCommand(name = entry.name, help = entry.help) {

    protected abstract val sender: CommandSender

    open class Entry(
        val name: String,
        val help: String,
        val alias: List<String> = emptyList(),
    )

    init {
        context { localization = CommandLocalization }
    }
}
