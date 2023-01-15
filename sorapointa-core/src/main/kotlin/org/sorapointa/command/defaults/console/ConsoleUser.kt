package org.sorapointa.command.defaults.console

import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import org.sorapointa.CoreBundle
import org.sorapointa.command.CommandLocalization
import org.sorapointa.command.ConsoleCommand
import org.sorapointa.command.ConsoleCommandSender
import org.sorapointa.command.utils.switchSet
import org.sorapointa.console.ConsoleUsers

class ConsoleUser(val sender: ConsoleCommandSender) : ConsoleCommand(
    sender,
    ConsoleUser,
    Option(printHelpOnEmptyArgs = true),
) {
    companion object : Entry(
        name = "consoleuser",
        helpKey = "sora.cmd.console.user.desc",
        alias = listOf("cslusr"),
    )

    private val username by option(
        names = arrayOf("--username", "-u"),
        help = CoreBundle.message("sora.cmd.console.user.opt.user", locale = sender.locale),
    )

    private val password by option(
        names = arrayOf("--password", "--pwd", "-p"),
        help = CoreBundle.message("sora.cmd.console.user.opt.pwd", locale = sender.locale),
    )

    enum class Operation {
        ADD, UPDATE, DELETE, LIST
    }

    private val operation by option(
        help = CoreBundle.message("sora.cmd.console.user.opt.operation", locale = sender.locale),
    ).switchSet(
        setOf("--add", "-a") to Operation.ADD,
        setOf("--update", "--upd", "-U") to Operation.UPDATE,
        setOf("--delete", "--del", "-d") to Operation.DELETE,
        setOf("--list", "--ls", "-l") to Operation.LIST,
    ).default(Operation.ADD)

    private suspend fun addOrUpdate(username: String?) {
        if (username == null) throw PrintMessage(CommandLocalization.missingOption("--username"))
        val sender = this@ConsoleUser.sender
        ConsoleUsers.addOrUpdate(
            username,
            password ?: run {
                sender.sendMessage(CoreBundle.message("sora.cmd.console.user.msg.empty.pwd", locale = sender.locale))
                ""
            },
        )
    }

    override suspend fun run() {
        when (operation) {
            Operation.UPDATE -> {
                addOrUpdate(username)
                sender.sendMessage(
                    CoreBundle.message("sora.cmd.console.user.msg.success.update", username, locale = sender.locale),
                )
                ConsoleUsers.save()
            }

            Operation.ADD -> {
                if (ConsoleUsers.data.users.contains(username)) {
                    sender.sendMessage(
                        CoreBundle.message("sora.cmd.console.user.msg.duplicate", username, locale = sender.locale),
                    )
                    return
                }
                addOrUpdate(username)
                sender.sendMessage(
                    CoreBundle.message("sora.cmd.console.user.msg.success.add", username, locale = sender.locale),
                )
                ConsoleUsers.save()
            }

            Operation.DELETE -> {
                val removed = ConsoleUsers.data.users.remove(username) != null
                if (removed) {
                    sender.sendMessage(
                        CoreBundle.message(
                            "sora.cmd.console.user.msg.success.remove",
                            username,
                            locale = sender.locale,
                        ),
                    )
                } else {
                    sender.sendMessage(
                        CoreBundle.message("sora.cmd.console.user.msg.nosuch", username, locale = sender.locale),
                    )
                }
            }

            Operation.LIST -> {
                val usrs = ConsoleUsers.data.users.keys
                sender.sendMessage(
                    CoreBundle.message(
                        "sora.cmd.console.user.msg.list",
                        usrs.size,
                        usrs.joinToString(),
                        locale = sender.locale,
                    ),
                )
            }
        }
    }
}
