package org.sorapointa.dispatch

import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import org.sorapointa.utils.MessageBundle
import java.util.*

internal const val BUNDLE = "messages.DispatchBundle"

object DispatchBundle : MessageBundle(BUNDLE) {
    @Nls
    @JvmStatic
    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any?,
        locale: Locale? = null,
    ): String = getString(key, *params, locale = locale)
}
