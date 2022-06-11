package org.sorapointa.console

import io.ktor.util.collections.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.DefaultParser
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.widget.AutosuggestionWidgets
import org.sorapointa.command.RemoteCommandSender
import org.sorapointa.utils.ModuleScope
import org.sorapointa.utils.addShutdownHook
import org.sorapointa.utils.resolveHome
import org.sorapointa.utils.resolveWorkDirectory
import java.io.OutputStream
import java.io.PrintStream

@Suppress("MemberVisibilityCanBePrivate")
internal object Console {
    private val terminal: Terminal = TerminalBuilder.terminal()

    private var reader: LineReader? = null

    internal fun initReader() {
        reader = LineReaderBuilder.builder()
            .appName("Sorapointa")
            .terminal(terminal)
            .parser(DefaultParser())
            .build().apply {
                AutosuggestionWidgets(this).enable()
                initHistory()
            }
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

    fun readln(): String = reader?.readLine("> ") ?: error("Reader not prepared")

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

    fun println(string: String?) = reader?.printAbove(string) ?: kotlin.io.println(string)

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
