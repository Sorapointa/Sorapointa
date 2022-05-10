package org.sorapointa.command.defaults

import org.sorapointa.command.CommandNode

val defaultsCommand: List<CommandNode> = listOf(
    CommandNode(Help) { sender -> Help(sender) },
)
