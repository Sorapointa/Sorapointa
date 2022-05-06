package org.sorapointa.command

import kotlinx.coroutines.runBlocking
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.types.int
import org.junit.jupiter.api.Test
import org.sorapointa.command.defaults.HelpCommand
import org.sorapointa.utils.I18nConfig
import org.sorapointa.utils.I18nManager
import java.io.File
import java.util.*

class CommandTest {
    @Test
    fun commandTest() {
        this::class.java.getResource(
            "/i18n/${Locale.ENGLISH.toLanguageTag()}.json"
        )?.also {
            runBlocking {
                I18nManager.registerLanguage(File(it.toURI()))
            }
        }
        this::class.java.getResource(
            "/i18n/${Locale.CHINA.toLanguageTag()}.json"
        )?.also {
            runBlocking {
                I18nManager.registerLanguage(File(it.toURI()))
                I18nConfig.reload()
            }
        }
        CommandManager.registerCommand(TestCommand)
        CommandManager.registerCommand(HelpCommand)
        println("Test command: help")
        CommandManager.invokeCommand(TestSender, "help")
        println("Test command: help 2")
        CommandManager.invokeCommand(TestSender, "help 2")
        println("Test command: help --help")
        CommandManager.invokeCommand(TestSender, "help --help")
        println("Test finish.")
    }
}

object TestSender : CommandSender() {
    override fun sendMessage(msg: String) {
        println(msg)
    }
}

object TestCommand : SorapointaCommand(
    name = "test",
    type = CommandSenderType.ADMIN,
    alias = arrayOf("te")
) {
    private val intArg by argument().int()

    override fun run() {
        println("Run success. intArg is $intArg")
    }
}
