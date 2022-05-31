package org.sorapointa.console

import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
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

    fun readln(): String = reader.readLine("> ")

    fun println(any: Any?) = println(any.toString())

    fun println() = reader.printAbove("")

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
