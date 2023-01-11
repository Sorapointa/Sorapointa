package org.sorapointa.command.defaults.general

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.arguments.optional
import moe.sdl.yac.parameters.options.flag
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.enum
import org.sorapointa.CoreBundle
import org.sorapointa.command.Command
import org.sorapointa.command.CommandSender
import org.sorapointa.command.ConsoleCommandSender
import org.sorapointa.game.Player
import org.sorapointa.game.data.PlayerDataTable.LOCALE_LENGTH_LIMIT
import org.sorapointa.utils.I18nConfig
import java.util.Locale as JavaLocale

class LocaleCommand(private val sender: CommandSender) : Command(sender, LocaleCommand) {

    companion object : Entry(
        name = "locale",
        helpKey = "sora.cmd.locale.desc",
        alias = listOf("lang"),
    )

    enum class Operation {
        VIEW, SET, LIST,
    }

    private val defaultOp = Operation.VIEW
    private val operation by argument(
        CoreBundle.message("sora.cmd.locale.arg.operation.name", locale = sender.locale),
        help = CoreBundle.message(
            "sora.cmd.locale.arg.operation.desc",
            Operation.values().joinToString { it.name },
            defaultOp.name,
            locale = sender.locale,
        ),
    ).enum<Operation>(ignoreCase = true).default(defaultOp)

    private val newValue by argument(
        CoreBundle.message("sora.cmd.locale.arg.new.value.name", locale = sender.locale),
        help = CoreBundle.message("sora.cmd.locale.arg.new.value.desc", locale = sender.locale),
    ).optional()

    private val force by option(
        "--force",
        "-F",
        help = CoreBundle.message("sora.cmd.locale.opt.force.desc", locale = sender.locale),
    ).flag(default = false)

    private suspend fun sendLocaleInfo(locale: JavaLocale?) {
        val langTag = locale?.toLanguageTag() ?: "NONE"
        sender.sendMessage(CoreBundle.message("sora.cmd.locale.msg.view", langTag, locale = sender.locale))
    }

    private suspend fun modifyLocaleInfo(modifyValue: suspend (JavaLocale) -> Unit) {
        val newValue = this.newValue // For smart cast
        if (newValue == null) {
            sender.sendMessage(CoreBundle.message("sora.cmd.locale.msg.new.value.missing", locale = sender.locale))
            return
        }
        if (newValue.length > LOCALE_LENGTH_LIMIT) {
            sender.sendMessage(
                CoreBundle.message("sora.cmd.locale.msg.new.value.toolong", LOCALE_LENGTH_LIMIT, locale = sender.locale),
            )
            return
        }
        val locale = JavaLocale.forLanguageTag(newValue)
        val found = CoreBundle.availableLocales().contains(locale)
        if (found && !force) {
            sender.sendMessage(CoreBundle.message("sora.cmd.locale.msg.new.value.notfound", locale = sender.locale))
            return
        }
        modifyValue(locale)
        sender.sendMessage(
            CoreBundle.message("sora.cmd.locale.msg.success", locale.toLanguageTag(), locale = sender.locale),
        )
    }

    private suspend inline fun sendAvailableList() = sender.sendMessage(CoreBundle.availableLocales().joinToString())

    override suspend fun run() {
        when (sender) {
            is Player -> when (operation) {
                Operation.VIEW -> sendLocaleInfo(sender.locale)
                Operation.SET -> modifyLocaleInfo { sender.locale = it }
                Operation.LIST -> sendAvailableList()
            }

            is ConsoleCommandSender -> when (operation) {
                Operation.VIEW -> sendLocaleInfo(I18nConfig.data.globalLocale)
                Operation.SET -> modifyLocaleInfo {
                    I18nConfig.data.globalLocale = it
                    I18nConfig.save()
                }
                Operation.LIST -> sendAvailableList()
            }

            else -> sender.sendMessage(CoreBundle.message("sora.cmd.locale.msg.unsupported", locale = sender.locale))
        }
    }
}
