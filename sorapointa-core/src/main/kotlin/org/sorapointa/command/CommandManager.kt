package org.sorapointa.command

import moe.sdl.yac.core.CommandResult
import moe.sdl.yac.core.PrintHelpMessage
import moe.sdl.yac.core.parseToArgs

object CommandManager {
    private val cmdMap = mutableMapOf<String, SorapointaCommand>()
    private val aliasMap = mutableMapOf<String, SorapointaCommand>()

    val cmdList: MutableSet<String>
        get() {
            return cmdMap.keys
        }

    private val logger = mu.KotlinLogging.logger {}

    fun registerCommand(command: SorapointaCommand) {
        val name = command.commandName

        cmdMap[name]?.also { logger.warn { "Command name $name conflict." } }
            ?: also { cmdMap[name] = command }

        command.alias.forEach { alias ->
            aliasMap[alias]?.also { logger.warn { "Alias name $alias conflict." } }
                ?: also { aliasMap[alias] = command }
        }
    }

    fun invokeCommand(sender: CommandSender, rawMsg: String) {
        val args = rawMsg.parseToArgs()
        val name = args[0]
        (cmdMap[name] ?: aliasMap[name])?.also { cmd ->
            if (sender.type >= cmd.type) {
                cmd.sender = sender
                val result = cmd.main(args.subList(1, args.count()))
                if (result is CommandResult.Error) {
                    result.userMessage?.also { sender.sendMessage(it) }
                    if (result.cause is PrintHelpMessage)
                        sender.sendMessage("Alias: ${cmd.alias.contentToString()}")
                }
            } else {
                sender.sendMessage("No permission.")
            }
        }
    }
}
