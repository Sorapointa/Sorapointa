package org.sorapointa.command.defaults.console

import org.sorapointa.SorapointaMain
import org.sorapointa.command.Command
import org.sorapointa.command.CommandSender
import org.sorapointa.command.ConsoleCommandSender
import kotlin.system.exitProcess

class Quit(private val sender: CommandSender) : Command(sender, Quit) {
    companion object : Entry(
        name = "quit",
        help = "sora.cmd.quit.desc",
        alias = listOf("exit")
    )

    override suspend fun run() {
        if (sender !is ConsoleCommandSender) return
        SorapointaMain.closeAll()
        exitProcess(0)
    }
}
