package org.sorapointa.command

import org.sorapointa.utils.LocaleAble
import java.util.Locale

abstract class CommandSender(
    override val locale: Locale?,
) : LocaleAble {
    /** An abstract function to send a message to the sender.
     *  @param msg The message to be sent.
     */
    abstract fun sendMessage(msg: String)
}
