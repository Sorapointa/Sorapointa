package org.sorapointa.command

import moe.sdl.yac.core.CommandResult
import moe.sdl.yac.core.parseToArgs

object CommandManager {
    private val cmdMap = mutableMapOf<String, SorapointaCommand>()

    fun registerCommand(command: SorapointaCommand) {
        val name = command.commandName

        cmdMap[name]?.also { println("Command name conflict.") }
            ?: also { cmdMap[name] = command }

        command.alias.forEach { alias ->
            cmdMap[alias]?.also { println("Alias name conflict.") }
                ?: also { cmdMap[alias] = command }
        }
    }

    fun invokeCommand(sender: CommandSender, rawMsg: String) {
        val args = rawMsg.parseToArgs()
        cmdMap[args[0]]?.also { cmd ->
            val permissions = sender.permissions
            if (permissions.contains("*") || permissions.contains(cmd.permission)) {
                cmd.sender = sender
                val result = cmd.main(args.subList(1, args.count()))
                if (result is CommandResult.Error)
                    result.userMessage?.also { sender.sendMessage(it) }
            } else {
                sender.sendMessage("No permission.")
            }
        }
    }
}
