package org.sorapointa.command.defaults.console

import org.sorapointa.command.ConsoleCommand
import org.sorapointa.command.ConsoleCommandSender
import kotlin.system.exitProcess

class Quit(sender: ConsoleCommandSender) : ConsoleCommand(sender, Quit) {
    companion object : Entry(
        name = "quit",
        helpKey = "sora.cmd.quit.desc",
        alias = listOf("exit")
    )

    override suspend fun run() {
        exitProcess(0)
    }
}
