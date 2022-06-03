package org.sorapointa.command

import kotlinx.coroutines.*
import moe.sdl.yac.core.CommandResult.Error
import moe.sdl.yac.core.CommandResult.Success
import moe.sdl.yac.core.PrintHelpMessage
import moe.sdl.yac.core.parseToArgs
import org.sorapointa.utils.ModuleScope
import org.sorapointa.utils.i18n
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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

    private var commandScope = ModuleScope("CommandManager")

    internal fun init(parentContext: CoroutineContext = EmptyCoroutineContext) {
        commandScope = ModuleScope("CommandManager", parentContext)
    }

    @Suppress("unused")
    fun registerCommand(entry: Command.Entry, creator: (CommandSender) -> Command) {
        registerCommand(CommandNode(entry, creator))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun registerCommand(commandNode: CommandNode) {
        val name = commandNode.entry.name
        val alias = commandNode.entry.alias

        cmdMap.putIfAbsent(name, commandNode)?.also {
            logger.warn { "Command name '$name' conflict." }
        }

        alias.forEach {
            aliasMap.putIfAbsent(it, commandNode)?.also {
                logger.warn { "Alias name '$alias' conflict." }
            }
        }
    }

    fun registerCommands(collection: Collection<CommandNode>): Unit =
        collection.forEach { registerCommand(it) }

    fun invokeCommand(
        sender: CommandSender,
        rawMsg: String,
    ): Job = commandScope.launch {
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
