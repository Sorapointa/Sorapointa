package org.sorapointa.command

import kotlinx.coroutines.*
import moe.sdl.yac.core.CommandResult.Error
import moe.sdl.yac.core.CommandResult.Success
import moe.sdl.yac.core.PrintHelpMessage
import moe.sdl.yac.core.parseToArgs
import org.sorapointa.utils.i18n
import java.util.concurrent.ConcurrentHashMap

private val logger = mu.KotlinLogging.logger {}

data class CommandNode(
    val entry: Command.Entry,
    val creator: (sender: CommandSender) -> Command,
)

object CommandManager {
    private val cmdMap: MutableMap<String, CommandNode> = ConcurrentHashMap()

    // A map to save the registered commands with alias.
    private val aliasMap: MutableMap<String, CommandNode> = ConcurrentHashMap()

    val commandEntries: List<Command.Entry> get() = cmdMap.entries.map { it.value.entry }

    private val commandExceptionHandler =
        CoroutineExceptionHandler { _, e -> logger.error(e) { "Caught Exception on CommandManager" } }
    private val commandContext = commandExceptionHandler + Dispatchers.Default + CoroutineName("CommandManager")

    fun registerCommand(entry: Command.Entry, creator: (CommandSender) -> Command) {
        registerCommand(CommandNode(entry, creator))
    }

    fun registerCommand(commandNode: CommandNode) {
        val name = commandNode.entry.name
        val alias = commandNode.entry.alias

        if (cmdMap[name] == null) {
            cmdMap[name] = commandNode
        } else logger.warn { "Command name '$name' conflict." }

        alias.forEach {
            if (aliasMap[it] == null) {
                aliasMap[it] = commandNode
            } else logger.warn { "Alias name '$alias' conflict." }
        }
    }

    fun registerCommands(collection: Collection<CommandNode>): Unit =
        collection.forEach { registerCommand(it) }

    fun invokeCommand(sender: CommandSender, rawMsg: String, scope: CoroutineScope =
        CoroutineScope(commandContext)) = scope.launch(commandContext) {
        if (rawMsg.isEmpty()) {
            sender.sendMessage("sora.cmd.manager.invoke.empty".i18n(locale = sender))
            return@launch
        }

        val args = rawMsg.parseToArgs()
        val mainCommand = args[0]

        val cmd = cmdMap[mainCommand] ?: aliasMap[mainCommand] ?: run {
            sender.sendMessage("sora.cmd.manager.invoke.error".i18n(mainCommand, locale = sender))
            return@launch
        }

        when (val result = cmd.creator(sender).main(args.drop(1))) {
            is Error -> {
                val msg = buildString {
                    append(result.userMessage)
                    if (result.cause is PrintHelpMessage && cmd.entry.alias.isNotEmpty()) {
                        append("sora.cmd.manager.alias".i18n(cmd.entry.alias.joinToString(), locale = sender))
                    }
                }
                sender.sendMessage(msg)
            }
            is Success -> {
                // pass
            }
        }

    }
}
