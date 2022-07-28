package org.sorapointa.utils

import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import java.util.*

internal const val BUNDLE = "messages.TestBundle"

object TestBundle : MessageBundle(BUNDLE) {
    @Nls
    @JvmStatic
    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any?,
        locale: Locale? = null
    ): String = getString(key, *params, locale = locale)
}
