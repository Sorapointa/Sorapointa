package org.sorapointa.command

import org.sorapointa.utils.LocaleAble

interface CommandSender : LocaleAble {

    /**
     * An abstract function to send a message to the sender.
     *
     * @param msg The message to be sent.
     */
    suspend fun sendMessage(msg: String)
}
