package org.sorapointa.command

import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.core.context
import org.sorapointa.utils.i18n

abstract class Command(
    sender: CommandSender,
    entry: Entry,
) : CliktCommand(name = entry.name, help = entry.help.i18n(locale = sender)) {
    /**
     * @param help, will be invoked with i18n
     */
    open class Entry(
        val name: String,
        val help: String,
        val alias: List<String> = emptyList(),
    )

    init {
        context { localization = CommandLocalization }
    }
}
