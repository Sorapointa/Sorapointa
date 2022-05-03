package org.sorapointa.command

abstract class CommandSender {
    var permissions = arrayOf("*")
    abstract fun sendMessage(msg: String)
}
