package org.sorapointa.command

import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.core.context
import org.jetbrains.annotations.PropertyKey
import org.sorapointa.BUNDLE
import org.sorapointa.CoreBundle
import org.sorapointa.game.Player

abstract class Command(
    sender: CommandSender,
    entry: Entry,
    option: Option = Option(),
) : CliktCommand(
    name = entry.name,
    help = CoreBundle.message(entry.helpKey, locale = sender.locale),
    invokeWithoutSubcommand = option.invokeWithoutSubCommand,
    printHelpOnEmptyArgs = option.printHelpOnEmptyArgs,
    allowMultipleSubcommands = option.allowMultipleSubcommands,
    treatUnknownOptionsAsArgs = option.treatUnknownOptionsAsArgs,
) {
    class Option(
        val invokeWithoutSubCommand: Boolean = false,
        val printHelpOnEmptyArgs: Boolean = false,
        val allowMultipleSubcommands: Boolean = false,
        val treatUnknownOptionsAsArgs: Boolean = false,
    )

    /**
     * @param helpKey, will be invoked with i18n
     */
    open class Entry(
        val name: String,
        @PropertyKey(resourceBundle = BUNDLE) val helpKey: String,
        val alias: List<String> = emptyList(),
        val permissionRequired: Int = 0
    )

    init {
        context { localization = CommandLocalization }
    }
}

abstract class ConsoleCommand(
    sender: ConsoleCommandSender,
    entry: Entry,
    option: Option = Option(),
) : Command(sender, entry, option)

abstract class PlayerCommand(
    sender: Player,
    entry: Entry,
    option: Option = Option(),
) : Command(sender, entry, option)
