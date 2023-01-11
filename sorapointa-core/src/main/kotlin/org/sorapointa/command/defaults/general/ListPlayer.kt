package org.sorapointa.command.defaults.general

import org.sorapointa.CoreBundle
import org.sorapointa.Sorapointa
import org.sorapointa.command.Command
import org.sorapointa.command.CommandSender

class ListPlayer(private val sender: CommandSender) : Command(sender, ListPlayer) {

    companion object : Entry(
        name = "listplayer",
        helpKey = "sora.cmd.list.player.desc",
        alias = listOf("list"),
        permissionRequired = 1,
    )

    override suspend fun run() {
        sender.sendMessage(
            CoreBundle.message(
                "sora.cmd.list.player.msg",
                Sorapointa.playerList.size,
                Sorapointa.playerList.joinToString { "${it.account.userName} (${it.uid})" },
                locale = sender.locale,
            ),
        )
    }
}
