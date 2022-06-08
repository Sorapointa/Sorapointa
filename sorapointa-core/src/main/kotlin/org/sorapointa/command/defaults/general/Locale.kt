package org.sorapointa.command.defaults.general

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.arguments.optional
import moe.sdl.yac.parameters.options.flag
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.enum
import org.sorapointa.command.Command
import org.sorapointa.command.CommandSender
import org.sorapointa.command.ConsoleCommandSender
import org.sorapointa.game.Player
import org.sorapointa.game.data.PlayerDataTable.LOCALE_LENGTH_LIMIT
import org.sorapointa.utils.I18nConfig
import org.sorapointa.utils.I18nManager
import org.sorapointa.utils.byPriority
import org.sorapointa.utils.i18n
import java.util.Locale as JavaLocale

class Locale(private val sender: CommandSender) : Command(sender, Locale) {

    companion object : Entry(
        name = "locale",
        help = "sora.cmd.locale.desc",
        alias = listOf("lang")
    )

    enum class Operation {
        VIEW, SET, LIST,
    }

    private val operation by argument(
        "sora.cmd.locale.arg.operation.name".i18n(locale = sender),
        help = "sora.cmd.locale.arg.operation.desc".i18n(locale = sender),
    ).enum<Operation>(ignoreCase = true).default(Operation.VIEW)

    private val newValue by argument(
        "sora.cmd.locale.arg.newvalue.name".i18n(locale = sender),
        help = "sora.cmd.locale.arg.newvalue.desc".i18n(locale = sender),
    ).optional()

    private val force by option(
        "--force", "-F",
        help = "sora.cmd.locale.opt.force.desc".i18n(locale = sender)
    ).flag(default = false)

    private suspend fun sendLocaleInfo(locale: JavaLocale?) {
        val langTag = locale?.toLanguageTag() ?: "NONE"
        sender.sendMessage("sora.cmd.locale.msg.view".i18n(langTag, locale = sender))
    }

    private suspend fun modifyLocaleInfo(modifyValue: suspend (JavaLocale) -> Unit) {
        val newValue = this.newValue // For smart cast
        if (newValue == null) {
            sender.sendMessage("sora.cmd.locale.msg.newvalue.missing".i18n(locale = sender))
            return
        }
        if (newValue.length > LOCALE_LENGTH_LIMIT) {
            sender.sendMessage("sora.cmd.locale.msg.newvalue.toolong".i18n(LOCALE_LENGTH_LIMIT, locale = sender))
            return
        }
        val locale = JavaLocale.forLanguageTag(newValue)
        val found = I18nManager.supportedLanguages.byPriority(listOf(locale))
        if (found == null && !force) {
            sender.sendMessage("sora.cmd.locale.msg.newvalue.notfound".i18n(locale = sender))
            return
        }
        modifyValue(locale)
        sender.sendMessage("sora.cmd.locale.msg.success".i18n(locale.toLanguageTag(), locale = sender))
    }

    private suspend inline fun sendAvailableList() = sender.sendMessage(I18nManager.supportedLanguages.joinToString())

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

            else -> sender.sendMessage("sora.cmd.locale.msg.unsupported".i18n(locale = sender))
        }
    }
}
