@file:Suppress("unused")

package org.sorapointa.console

import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.parameters.arguments.Argument
import moe.sdl.yac.parameters.groups.ParameterGroup
import moe.sdl.yac.parameters.options.Option
import org.jline.builtins.Completers.TreeCompleter.Node
import org.jline.builtins.Completers.TreeCompleter.node
import org.sorapointa.utils.uncheckedCast

private val cliktClazz = CliktCommand::class.java

@Suppress("NOTHING_TO_INLINE")
private inline fun field(name: String) = cliktClazz.getDeclaredField(name).apply {
    trySetAccessible()
} ?: throw NoSuchFieldException(name)

private val subcommands = field("_subcommands")
private val options = field("_options")
private val arguments = field("_arguments")
private val groups = field("_groups")

private fun CliktCommand.subcommands(): List<CliktCommand> = subcommands.get(this).uncheckedCast()
private fun CliktCommand.options(): List<Option> = options.get(this).uncheckedCast()
private fun CliktCommand.arguments(): List<Argument> = arguments.get(this).uncheckedCast()
private fun CliktCommand.groups(): List<ParameterGroup> = groups.get(this).uncheckedCast()

internal fun CliktCommand.toCompleterNodes(): List<Node> =
    subcommands().map { node(it.commandName, it.toCompleterNodes()) } +
        options().flatMap { it.names + it.secondaryNames }.map { node(it) } +
        groups().mapNotNull { it.groupName }.map { node(it) }
