package org.sorapointa.command

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
    permission = "",
    alias = arrayOf("te")
) {
    override fun run() {
        println("Run success.")
    }
}
