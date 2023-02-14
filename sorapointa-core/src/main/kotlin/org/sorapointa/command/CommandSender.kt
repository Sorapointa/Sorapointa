package org.sorapointa.command

import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import org.sorapointa.BUNDLE
import org.sorapointa.CoreBundle
import org.sorapointa.utils.LocaleAble
import java.util.*

interface CommandSender : LocaleAble {

    /**
     * An abstract function to send a message to the sender.
     *
     * @param msg The message to be sent.
     */
    suspend fun sendMessage(msg: String)
}

@Nls
internal suspend inline fun CommandSender.sendLocaled(
    @PropertyKey(resourceBundle = BUNDLE) key: String,
    vararg params: Any?,
) = sendMessage(CoreBundle.message(key, *params, locale = this.locale))

@Nls
@Suppress("NOTHING_TO_INLINE")
internal inline fun CommandSender.localed(
    @PropertyKey(resourceBundle = BUNDLE) key: String,
    vararg params: Any?,
): String = CoreBundle.message(key, *params, locale = this.locale)
