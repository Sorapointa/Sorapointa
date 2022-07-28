package org.sorapointa.utils

import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.text.MessageFormat
import java.util.*

/**
 * Provides internationalization (i18n) support through `.properties` files. Property files are simple key-value stores
 * that contains a mapping of message keys to its translated message.
 *
 * Example:
 * ```properties
 * user.not.found=User not found
 * server.error=Server internal error
 * ```
 * Inheriting [MessageBundle] gives access to the resource bundle provided by [bundlePath].
 *
 * Example:
 * ```Kotlin
 * internal const val BUNDLE = "messages.CoreBundle"
 *
 * object CoreBundle : MessageBundle(BUNDLE) {
 *     @Nls
 *     @JvmStatic
 *     fun message(
 *         @PropertyKey(resourceBundle = BUNDLE) key: String,
 *         vararg params: Any?,
 *         locale: Locale? = null
 *     ): String = getString(key, *params, locale = locale)
 * }
 * ```
 *
 * Retrieving a property can easily be done by calling:
 * ```Kotlin
 * CoreBundle.message("user.not.found")
 * ```
 *
 * The [bundlePath] is relative to the resources folder in the current source root. In the example above the properties
 * file should thus be placed as `SOURCE_ROOT/resources/messages/CoreBundle.properties`.
 *
 * The default (Root) bundle should always be in English. More languages can be added by adding suffixes to the property
 * files. For example, adding support for Spanish can be done by adding a new property file `CoreBundle_es.properties`
 * that contains the same key as the default bundle but with its values translated to Spanish. Regional bundles can also
 * be created in a similar matter, e.g. `CoreBundle_es_ES.properties`.
 *
 * Retrieving messages is done in a hierarchical manner. In our example the hierarchy would be
 * `CoreBundle_es_ES -> CoreBundle_es -> CoreBundle`. When retrieving a message for locale `es_ES` the [MessageBundle]
 * will first check its own bundle, if it can't find anything it will go down the hierarchy and repeat this process.
 * Every key should therefore be declared in the default bundle.
 *
 * Messages stored in property files can be formatted as described by [MessageFormat], creating a property that has
 * parameters can for example be done like:
 * ```properties
 * user.not.found=User {0} not found
 * ```
 * When retrieving the messages the arguments can be passed:
 * ```Kotlin
 * CoreBundle.message("user.not.found", "UserName")
 * ```
 * @see ResourceBundle
 * @see MessageFormat
 */
abstract class MessageBundle(private val bundlePath: String) {
    private fun control() = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES)

    /**
     * Implementations of [MessageBundle] should create a delegator to this message and apply [PropertyKey] to the
     * parameter passed into [key] to get better IDE support.
     *
     * @param key The property key to retrieve
     * @param params Params for the message to retrieve, the amount of params should be equal to the amount of
     * placeholders in the message
     * @param locale Can be used to overwrite the locale to retrieve the message for. By default, the locale is set to
     * [globalLocale] which can be configured by a config file.
     */
    @Nls
    protected fun getString(@NonNls key: String, vararg params: Any?, locale: Locale? = null): String {
        val bundle = ResourceBundle.getBundle(bundlePath, locale ?: globalLocale, control())
        return MessageFormat.format(bundle.getString(key), *params)
    }

    /**
     * Gets all available locales that are provided by this [MessageBundle], not that it is still possible for other
     * bundles to support different locales.
     */
    fun availableLocales(): Set<Locale> {
        val localesWithResource = mutableSetOf<Locale>()
        for (locale in Locale.getAvailableLocales()) {
            if (locale == Locale.ROOT) {
                localesWithResource.add(DEFAULT_LOCALE)
                continue
            }
            val bundleLocale = ResourceBundle.getBundle(bundlePath, locale, control()).locale
            if (bundleLocale != Locale.ROOT) localesWithResource.add(bundleLocale)
        }
        return localesWithResource
    }
}
