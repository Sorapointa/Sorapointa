package org.sorapointa.command

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.types.int
import org.junit.jupiter.api.Test
import org.sorapointa.command.defaults.HelpCommand

class CommandTest {
    @Test
    fun onCommandTest() {
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
    val intArg by argument().int()

    override fun run() {
        println("Run success. intArg is $intArg")
    }
}
