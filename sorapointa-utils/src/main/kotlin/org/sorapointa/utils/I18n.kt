package org.sorapointa.utils

import org.sorapointa.data.provider.ReadOnlyFilePersist
import java.io.File
import java.io.Serializable
import java.util.*

private val supportedLocales = arrayOf("en-US")

@kotlinx.serialization.Serializable
data class I18nConfig(
    val locale: String = supportedLocales.first(),
)

private val config = File("./conf/i18n.json").let { f ->
    f.parentFile?.mkdirs()
    f.createNewFile()
    object : ReadOnlyFilePersist<I18nConfig>(f, I18nConfig()) {}
}.data

val locale: Locale = Locale.Builder().setLanguageTag(config.locale).build().let {
    if (it.toLanguageTag() !in supportedLocales) {
        // warn?
        Locale.US
    } else it
}

val localeStr get() = locale.toLanguageTag().lowercase()

private val currentBundle = ResourceBundle.getBundle("assets.lang.i18n", locale)

fun String.i18n(locale: Locale? = null) = runCatching {
    locale?.let {
        ResourceBundle.getBundle("assets.lang.i18n", locale).getString(this)
    } ?: currentBundle.getString(this) ?: this
}.getOrElse { this }

fun String.i18n(vararg args: Serializable?) = i18n().replaceWithOrder(*args)
