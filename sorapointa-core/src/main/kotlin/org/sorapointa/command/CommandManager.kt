package org.sorapointa.command

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import moe.sdl.yac.core.CommandResult
import moe.sdl.yac.core.CommandResult.Error
import moe.sdl.yac.core.CommandResult.Success
import moe.sdl.yac.core.PrintHelpMessage
import moe.sdl.yac.core.parseToArgs
import org.sorapointa.CoreBundle
import org.sorapointa.game.Player
import org.sorapointa.utils.ModuleScope
import org.sorapointa.utils.suggestTypo
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = mu.KotlinLogging.logger {}

abstract class AbstractCommandNode<TSender : CommandSender>(
    val entry: Command.Entry,
    val creator: (sender: TSender) -> Command,
)

class CommandNode(
    entry: Command.Entry,
    creator: (CommandSender) -> Command
) : AbstractCommandNode<CommandSender>(entry, creator)

class ConsoleCommandNode(
    entry: Command.Entry,
    creator: (ConsoleCommandSender) -> Command
) : AbstractCommandNode<ConsoleCommandSender>(entry, creator)

class PlayerCommandNode(
    entry: Command.Entry,
    creator: (Player) -> Command
) : AbstractCommandNode<Player>(entry, creator)

object CommandManager {
    val commandMap: Map<String, AbstractCommandNode<*>>
        get() = cmdMap

    private val cmdMap: MutableMap<String, AbstractCommandNode<*>> = ConcurrentHashMap()

    // A map to save the registered commands with alias.
    private val aliasMap: MutableMap<String, AbstractCommandNode<*>> = ConcurrentHashMap()

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
    fun registerCommand(commandNode: AbstractCommandNode<*>) {
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

    fun registerCommands(collection: Collection<AbstractCommandNode<*>>): Unit =
        collection.forEach { registerCommand(it) }

    fun invokeCommand(
        sender: CommandSender,
        rawMsg: String,
    ): Job = commandScope.launch {
        if (rawMsg.isEmpty()) {
            sender.sendMessage(
                CoreBundle.message("sora.cmd.manager.invoke.empty", locale = sender.locale)
            )
            return@launch
        }

        val args = rawMsg.parseToArgs()
        val mainCommand = args[0]

        val cmd = cmdMap[mainCommand] ?: aliasMap[mainCommand] ?: run {
            val mainTypo = suggestTypo(mainCommand, cmdMap.keys.toList())
            if (mainTypo == null) {
                sender.sendMessage(
                    CoreBundle.message("sora.cmd.manager.invoke.error", mainCommand, locale = sender.locale)
                )
            } else {
                sender.sendMessage(
                    CoreBundle.message(
                        "sora.cmd.manager.invoke.typo.suggest",
                        mainCommand, mainTypo, locale = sender.locale
                    )
                )
            }
            return@launch
        }

        val result: CommandResult = run {
            if (sender is Player && sender.account.permissionLevel < cmd.entry.permissionRequired) {
                return@run Error(
                    null, userMessage = CoreBundle.message("sora.cmd.no.permission", locale = sender.locale)
                )
            }
            when (cmd) {
                is CommandNode -> {
                    cmd.creator(sender).execute(args)
                }

                is ConsoleCommandNode -> {
                    if (sender is ConsoleCommandSender) {
                        cmd.creator(sender).execute(args)
                    } else {
                        Error(null, userMessage = CoreBundle.message("sora.cmd.no.permission", locale = sender.locale))
                    }
                }

                is PlayerCommandNode -> {
                    if (sender is Player) {
                        cmd.creator(sender).execute(args)
                    } else {
                        Error(null, userMessage = CoreBundle.message("sora.cmd.is.not.player", locale = sender.locale))
                    }
                }

                else -> Error(null, userMessage = CoreBundle.message("server.error", locale = sender.locale))
            }
        }

        when (result) {
            is Error -> {
                val msg = buildString {
                    append(result.userMessage)
                    if (result.cause is PrintHelpMessage && cmd.entry.alias.isNotEmpty()) {
                        append(
                            CoreBundle.message(
                                "sora.cmd.manager.alias", cmd.entry.alias.joinToString(), locale = sender.locale
                            )
                        )
                    }
                }
                sender.sendMessage(msg)
            }

            is Success -> {
                // pass
            }
        }
    }

    /**
     * @param mainCommand main command or alias
     * @return [Boolean] has or not
     */
    fun hasCommand(mainCommand: String): Boolean =
        cmdMap.containsKey(mainCommand) || aliasMap.containsKey(mainCommand)
}

private suspend fun Command.execute(args: List<String>) = this.main(args.drop(1))
