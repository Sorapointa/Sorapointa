package org.sorapointa.command.defaults

import org.sorapointa.command.*
import org.sorapointa.command.defaults.console.*
import org.sorapointa.command.defaults.general.*

val defaultsCommand: List<AbstractCommandNode<*>> = listOf(
    CommandNode(Help) { sender -> Help(sender) },
    CommandNode(ListPlayer) { sender -> ListPlayer(sender) },
    CommandNode(LocaleCommand) { sender -> LocaleCommand(sender) },
    CommandNode(Version) { sender -> Version(sender) },
    ConsoleCommandNode(Quit) { sender -> Quit(sender) },
    ConsoleCommandNode(ConsoleUser) { sender -> ConsoleUser(sender) },
)
