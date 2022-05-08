package org.sorapointa.command

import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.core.context

/** The base class for all commands.
 *  @param name The name of the command.
 *  @param help The help info when use the "command -h".
 *  @param alias The alias for the command.
 *  @param type The sender type which the command required.
 */
abstract class SorapointaCommand(
    name: String,
    help: String = "",
    val alias: Array<String> = emptyArray(),
    val type: CommandSenderType = CommandSenderType.ADMIN,
) : CliktCommand(name = name, help = help) {
    // Let the clikt use the i18n.
    init {
        context { localization = CommandLocalization }
    }

    // TODO: 2022/5/7 A better way to set the sender.
    var sender: CommandSender? = null
}
