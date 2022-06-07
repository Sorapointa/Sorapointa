package org.sorapointa.command.defaults.general

import org.sorapointa.Sorapointa
import org.sorapointa.command.Command
import org.sorapointa.command.CommandSender
import org.sorapointa.utils.i18n

class ListPlayer(private val sender: CommandSender) : Command(sender, ListPlayer) {

    companion object : Entry(
        name = "listplayer",
        help = "sora.cmd.listplayer.desc",
        alias = listOf("list"),
        permissionRequired = 1u
    )

    override suspend fun run() {
        sender.sendMessage(
            "sora.cmd.listplayer.msg".i18n(
                Sorapointa.playerList.size,
                Sorapointa.playerList.joinToString { "${it.account.userName} (${it.uid})" }, locale = sender
            )
        )
    }
}
