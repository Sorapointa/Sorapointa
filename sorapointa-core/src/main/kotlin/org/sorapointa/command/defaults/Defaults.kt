package org.sorapointa.command.defaults

import org.sorapointa.command.CommandNode
import org.sorapointa.command.defaults.console.Quit

val defaultsCommand: List<CommandNode> = listOf(
    CommandNode(Help) { sender -> Help(sender) },
    CommandNode(Quit) { sender -> Quit(sender) }
)
