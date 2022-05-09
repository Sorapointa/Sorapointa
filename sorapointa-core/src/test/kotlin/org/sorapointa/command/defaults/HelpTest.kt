package org.sorapointa.command.defaults

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.sorapointa.command.Command
import org.sorapointa.command.CommandManager
import org.sorapointa.command.CommandNode
import org.sorapointa.command.CommandSender
import org.sorapointa.utils.I18nConfig
import org.sorapointa.utils.I18nManager
import org.sorapointa.utils.languagesDirectory
import java.util.*
import kotlin.system.measureTimeMillis

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HelpTest {
    @BeforeAll
    fun init() = runBlocking {
        I18nConfig.init()
        I18nManager.registerLanguagesDirectory(languagesDirectory)
        CommandManager.registerCommands(defaultsCommand)
        CommandManager.registerCommands(
            buildList {
                repeat(50) {
                    add(CommandNode(generateEntry()) { TestCommand(TestSender, generateEntry()) })
                }
            }
        )
    }

    fun generateEntry() = Command.Entry(
        "abcdefghijklmnopqrstuvwxyz".toList().shuffled().subList(0, (4..10).random()).joinToString(""),
        help = "abcdefghijklmnopqrstuvwxyz".toList().shuffled().subList(0, (0..5).random()).joinToString("")
    )

    class TestCommand(override val sender: CommandSender, private val entry: Entry) : Command(entry) {
        override fun run() {
            println("${entry.name} run!")
        }
    }

    object TestSender : CommandSender(Locale.CHINESE) {
        override fun sendMessage(msg: String) {
            println(msg)
        }
    }

    @Test
    fun `print help`() {
        CommandManager.invokeCommand(TestSender, "help")
    }

    @Test
    fun `print exceed`() {
        CommandManager.invokeCommand(TestSender, "help 114514191")
    }

    @Test
    fun `print with page`() {
        val ms = measureTimeMillis {
            CommandManager.invokeCommand(TestSender, "help 10 -s 2")
        }
        println("Costed $ms ms")
    }

    @Test
    fun `alias test`() {
        CommandManager.invokeCommand(TestSender, "? 10 -s 2")
    }

    @Test
    fun `-h test`() {
        CommandManager.invokeCommand(TestSender, "help -h")
    }
}
