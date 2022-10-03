package org.sorapointa.command.defaults

import org.sorapointa.command.AbstractCommandNode
import org.sorapointa.command.CommandNode
import org.sorapointa.command.ConsoleCommandNode
import org.sorapointa.command.defaults.console.ConsoleUser
import org.sorapointa.command.defaults.console.Quit
import org.sorapointa.command.defaults.general.Help
import org.sorapointa.command.defaults.general.ListPlayer
import org.sorapointa.command.defaults.general.LocaleCommand
import org.sorapointa.command.defaults.general.Version

val defaultsCommand: List<AbstractCommandNode<*>> = listOf(
    CommandNode(Help) { sender -> Help(sender) },
    CommandNode(ListPlayer) { sender -> ListPlayer(sender) },
    CommandNode(LocaleCommand) { sender -> LocaleCommand(sender) },
    CommandNode(Version) { sender -> Version(sender) },
    ConsoleCommandNode(Quit) { sender -> Quit(sender) },
    ConsoleCommandNode(ConsoleUser) { sender -> ConsoleUser(sender) },
)
