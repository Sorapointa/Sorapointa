package org.sorapointa.console

import io.ktor.util.collections.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jline.builtins.Completers.TreeCompleter
import org.jline.builtins.Completers.TreeCompleter.node
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.LineReaderImpl
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.widget.AutosuggestionWidgets
import org.sorapointa.command.*
import org.sorapointa.utils.*
import java.io.OutputStream
import java.io.PrintStream
import java.util.*
import kotlin.reflect.jvm.ExperimentalReflectionOnLambdas
import kotlin.reflect.jvm.reflect
import kotlin.reflect.typeOf

@Suppress("MemberVisibilityCanBePrivate")
internal object Console {
    private val terminal: Terminal = TerminalBuilder.terminal()

    private var reader: LineReader? = null

    private object FakeSender : ConsoleCommandSender() {
        override suspend fun sendMessage(msg: String) {}
        override val locale: Locale = Locale.ENGLISH
    }

    internal fun initReader() {
        reader = LineReaderBuilder.builder()
            .appName("Sorapointa")
            .terminal(terminal)
            .highlighter(SoraHighlighter)
            .build().apply {
                AutosuggestionWidgets(this).enable()
                initHistory()
            }
    }

    @OptIn(ExperimentalReflectionOnLambdas::class)
    internal fun setupCompletion() {
        val completions by lazy {
            CommandManager.commandMap.filter { (_, node) ->
                val type = node.creator.reflect()?.parameters?.firstOrNull()?.type
                type == typeOf<CommandSender>() || type == typeOf<ConsoleCommandSender>()
            }.flatMap { (_, node) ->
                val creator = node.creator.uncheckedCast<(sender: CommandSender) -> Command>()
                val nodes = creator(FakeSender).toCompleterNodes()
                buildList {
                    add(node.entry.name)
                    addAll(node.entry.alias)
                }.map {
                    node(it, *nodes.toTypedArray())
                }
            }.let { TreeCompleter(it) }
        }
        (reader as? LineReaderImpl)?.completer = completions
    }

    private const val HISTORY_FILE = ".sorapointa_history"

    private fun LineReader.initHistory() {
        setVariable(
            LineReader.HISTORY_FILE,
            resolveHome(HISTORY_FILE)
                ?: resolveWorkDirectory(HISTORY_FILE)
        )
        DefaultHistory(this).apply {
            addShutdownHook {
                save()
            }
        }
    }

    private val scope = ModuleScope("RemoteConsole", dispatcher = Dispatchers.IO)

    internal val consoleUsers = ConcurrentSet<RemoteCommandSender>()

    fun readln(prompt: String = "> "): String = reader?.readLine(prompt) ?: error("Reader not prepared")

    fun println(any: Any?) {
        if (consoleUsers.isNotEmpty()) {
            scope.launch {
                consoleUsers.removeIf { !it.isActive }
                consoleUsers.forEach {
                    it.sendMessage(any.toString())
                }
            }
        }
        reader?.printAbove(any.toString()) ?: kotlin.io.println(any)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun println(string: String?) = println(any = string)

    internal fun redirectToJLine() {
        System.setErr(JLineRedirector)
        System.setOut(JLineRedirector)
    }

    internal fun redirectToNull() {
        val out = PrintStream(OutputStream.nullOutputStream())
        System.setOut(out)
        System.setErr(out)
    }
}
