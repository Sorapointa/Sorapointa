package org.sorapointa.utils

import org.sorapointa.data.provider.ReadOnlyFilePersist
import java.io.File
import java.io.Serializable
import java.util.*

private val supportedLocales = arrayOf("en-US")

private val logger = mu.KotlinLogging.logger { }

object I18nConfig : ReadOnlyFilePersist<I18nConfig.Data>(
    location = File(configDirectory, "i18n.json"),
    default = Data()
) {
    @kotlinx.serialization.Serializable
    data class Data(
        val locale: String = supportedLocales.first(),
    )
}

private val DEFAULT_LOCALE: Locale = Locale.US

val locale: Locale = Locale.Builder().setLanguageTag(I18nConfig.data.locale).build().let {
    if (it.toLanguageTag() !in supportedLocales) {
        logger.warn { "Language $it is not supported, switching to $DEFAULT_LOCALE" }
        DEFAULT_LOCALE
    } else it
}

val localeStr get() = locale.toLanguageTag().lowercase()

private val currentBundle = ResourceBundle.getBundle("assets.lang.i18n", locale)

fun String.i18n(locale: Locale? = null) = runCatching {
    locale?.let {
        ResourceBundle.getBundle("assets.lang.i18n", locale).getString(this)
    } ?: currentBundle.getString(this) ?: this
}.getOrElse { this }

/**
 * @receiver i18n key, dot-styled path, like 'sorapointa.command.help.usage'
 * @param args all args to pass to placeholder
 * @return replaced string
 */
fun String.i18n(vararg args: Serializable?): String = i18n().replaceWithOrder(*args)
