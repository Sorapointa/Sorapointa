package org.sorapointa.command

/** An abstract class for the command sender.
 * @param type Then type of the sender. Default is SERVER.
 * @see CommandSenderType
 */
abstract class CommandSender(
    val type: CommandSenderType = CommandSenderType.SERVER
) {
    /** An abstract function to send a message to the sender.
     *  @param msg The message to be sent.
     */
    abstract fun sendMessage(msg: String)
}

/** An enum of sender type, in the order of permission levels. */
enum class CommandSenderType {
    PLAYER,
    ADMIN,
    SERVER,
}
