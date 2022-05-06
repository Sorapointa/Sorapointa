package org.sorapointa.command

import moe.sdl.yac.core.CommandResult
import moe.sdl.yac.core.PrintHelpMessage
import moe.sdl.yac.core.parseToArgs
import org.sorapointa.utils.i18n

object CommandManager {
    private val cmdMap = mutableMapOf<String, SorapointaCommand>()
    private val aliasMap = mutableMapOf<String, SorapointaCommand>()

    val cmdList get() = cmdMap.values.toList()

    private val logger = mu.KotlinLogging.logger {}

    fun registerCommand(command: SorapointaCommand) {
        val name = command.commandName

        cmdMap[name]?.also { logger.warn { "Command name '$name' conflict." } }
            ?: also { cmdMap[name] = command }

        command.alias.forEach { alias ->
            aliasMap[alias]?.also { logger.warn { "Alias name '$alias' conflict." } }
                ?: also { aliasMap[alias] = command }
        }
    }

    fun invokeCommand(sender: CommandSender, rawMsg: String) {
        val args = rawMsg.parseToArgs()
        val name = args[0]
        (cmdMap[name] ?: aliasMap[name])?.also { cmd ->
            if (sender.type < cmd.type) {
                sender.sendMessage(
                    "clikt.usage.error".i18n("sora.cmd.manager.no.permission".i18n())
                )
                return@also
            }
            cmd.sender = sender
            val result = cmd.main(args.subList(1, args.count()))
            if (result !is CommandResult.Error) return@also
            sender.sendMessage(buildString {
                append(result.userMessage)
                if (result.cause is PrintHelpMessage && cmd.alias.isNotEmpty())
                    append("sora.cmd.manager.alias".i18n(cmd.alias.contentToString()))

            })
        }
    }
}
