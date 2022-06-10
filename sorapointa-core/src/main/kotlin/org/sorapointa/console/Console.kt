package org.sorapointa.console

import io.ktor.util.collections.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.sorapointa.command.RemoteCommandSender
import org.sorapointa.utils.ModuleScope
import java.io.OutputStream
import java.io.PrintStream

@Suppress("MemberVisibilityCanBePrivate")
internal object Console {
    private val terminal: Terminal = TerminalBuilder.builder()
        .name("Sorapointa")
        .system(true)
        .encoding(Charsets.UTF_8)
        .build()

    private val reader = LineReaderBuilder.builder()
        .terminal(terminal)
        .appName("Sorapointa")
        .history(DefaultHistory())
        .build()

    private val scope = ModuleScope("RemoteConsole", dispatcher = Dispatchers.IO)

    internal val consoleUsers = ConcurrentSet<RemoteCommandSender>()

    fun readln(): String = reader.readLine("> ")

    fun println(any: Any?) {
        if (consoleUsers.isNotEmpty()) {
            consoleUsers.removeIf { !it.isActive }
            consoleUsers.forEach {
                scope.launch {
                    it.sendMessage(any.toString())
                }
            }
        }
        reader.printAbove(any.toString())
    }

    fun println(string: String?) = reader.printAbove(string)

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
