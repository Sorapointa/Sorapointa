package org.sorapointa.command

import moe.sdl.yac.core.CommandResult
import moe.sdl.yac.core.PrintHelpMessage
import moe.sdl.yac.core.parseToArgs
import org.sorapointa.utils.i18n

/** An object to manage the commands. */
object CommandManager {
    // A map to save the registered commands with name.
    private val cmdMap = mutableMapOf<String, SorapointaCommand>()

    // A map to save the registered commands with alias.
    private val aliasMap = mutableMapOf<String, SorapointaCommand>()

    // Used to get the command list.
    val cmdList get() = cmdMap.values.toList()

    // Just the logger.
    private val logger = mu.KotlinLogging.logger {}

    /** A function to register a command.
     *  @param command The object extends SorapointaCommand.
     */
    fun registerCommand(command: SorapointaCommand) {
        val name = command.commandName

        cmdMap[name]?.also { logger.warn { "Command name '$name' conflict." } }
            ?: also { cmdMap[name] = command }

        command.alias.forEach { alias ->
            aliasMap[alias]?.also { logger.warn { "Alias name '$alias' conflict." } }
                ?: also { aliasMap[alias] = command }
        }
    }

    /** A function to invoke a command which registered.
     *  @param sender The sender of the command.
     *  @param rawMsg The original message of the execution command.
     */
    fun invokeCommand(sender: CommandSender, rawMsg: String) {
        // Get the command name from rawMsg.
        val args = rawMsg.parseToArgs()
        val name = args[0]
        // Get the command by the name.
        (cmdMap[name] ?: aliasMap[name])?.also { cmd ->
            // Check the type (i.e. permissions)
            if (sender.type < cmd.type) {
                sender.sendMessage(
                    "clikt.usage.error".i18n("sora.cmd.manager.no.permission".i18n())
                )
                return@also
            }
            // Set the sender.
            cmd.sender = sender
            // Invoke the command.
            val result = cmd.main(args.subList(1, args.count()))
            // Handle errors.
            if (result !is CommandResult.Error) return@also
            sender.sendMessage(
                buildString {
                    append(result.userMessage)
                    // Add alias to the help.
                    if (result.cause is PrintHelpMessage && cmd.alias.isNotEmpty())
                        append("sora.cmd.manager.alias".i18n(cmd.alias.contentToString()))
                }
            )
        }
    }
}
