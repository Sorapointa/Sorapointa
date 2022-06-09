package org.sorapointa.command.defaults.console

import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import org.sorapointa.command.CommandLocalization
import org.sorapointa.command.ConsoleCommand
import org.sorapointa.command.ConsoleCommandSender
import org.sorapointa.command.utils.switchSet
import org.sorapointa.console.ConsoleUsers
import org.sorapointa.utils.i18n

class ConsoleUser(val sender: ConsoleCommandSender) : ConsoleCommand(
    sender,
    ConsoleUser,
    Option(printHelpOnEmptyArgs = true)
) {
    companion object : Entry(
        name = "consoleuser",
        help = "sora.cmd.consoleuser.desc",
        alias = listOf("cslusr")
    )

    private val username by option(
        names = arrayOf("--username", "-u"),
        help = "sora.cmd.consoleuser.opt.user".i18n(locale = sender)
    )

    private val password by option(
        names = arrayOf("--password", "--pwd", "-p"),
        help = "sora.cmd.consoleuser.opt.pwd".i18n(locale = sender)
    )

    enum class Operation {
        ADD, UPDATE, DELETE, LIST
    }

    private val operation by option(
        help = "sora.cmd.consoleuser.opt.operation".i18n(locale = sender)
    ).switchSet(
        setOf("--add", "-a") to Operation.ADD,
        setOf("--update", "--upd", "-U") to Operation.UPDATE,
        setOf("--delete", "--del", "-d") to Operation.DELETE,
        setOf("--list", "--ls", "-l") to Operation.LIST,
    ).default(Operation.ADD)

    private suspend fun addOrUpdate(username: String?) {
        if (username == null) throw PrintMessage(CommandLocalization.missingOption("--username"))
        val sender = this@ConsoleUser.sender
        ConsoleUsers.addOrUpdate(username, password ?: run {
            sender.sendMessage("sora.cmd.consoleuser.msg.emptypwd".i18n(locale = sender))
            ""
        })
    }

    override suspend fun run() {
        when (operation) {
            Operation.UPDATE -> {
                addOrUpdate(username)
                sender.sendMessage("sora.cmd.consoleuser.msg.success.update".i18n(username, locale = sender))
                ConsoleUsers.save()
            }
            Operation.ADD -> {
                if (ConsoleUsers.data.users.contains(username)) {
                    sender.sendMessage("sora.cmd.consoleuser.msg.duplicate".i18n(username, locale = sender))
                    return
                }
                addOrUpdate(username)
                sender.sendMessage("sora.cmd.consoleuser.msg.success.add".i18n(username, locale = sender))
                ConsoleUsers.save()
            }
            Operation.DELETE -> {
                val removed = ConsoleUsers.data.users.remove(username) != null
                if (removed) {
                    sender.sendMessage("sora.cmd.consoleuser.msg.success.remove".i18n(username, locale = sender))
                } else sender.sendMessage("sora.cmd.consoleuser.msg.nosuch".i18n(username, locale = sender))
            }
            Operation.LIST -> {
                val usrs = ConsoleUsers.data.users.keys
                sender.sendMessage(
                    "sora.cmd.consoleuser.msg.list".i18n(
                        usrs.size,
                        usrs.joinToString(),
                        locale = sender,
                    )
                )
            }
        }

    }
}
