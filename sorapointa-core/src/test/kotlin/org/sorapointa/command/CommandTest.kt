package org.sorapointa.command

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.types.int
import org.junit.jupiter.api.Test

class CommandTest {
    @Test
    fun onCommandTest() {
        CommandManager.registerCommand(TestCommand)
        CommandManager.invokeCommand(object : CommandSender() {
            override fun sendMessage(msg: String) {
                println(msg)
            }
        }, "te --help")
    }
}

object TestCommand : SorapointaCommand(
    name = "test",
    type = CommandSenderType.ADMIN,
    alias = arrayOf("te")
) {
    val intArg by argument().int()

    override fun run() {
        println("Run success.")
    }
}
