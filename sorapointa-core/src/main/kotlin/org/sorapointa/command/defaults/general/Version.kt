package org.sorapointa.command.defaults.general

import org.sorapointa.command.Command
import org.sorapointa.command.CommandSender
import org.sorapointa.config.BUILD_BRANCH
import org.sorapointa.config.COMMIT_HASH
import org.sorapointa.config.VERSION

class Version(private val sender: CommandSender) : Command(sender, Version) {

    companion object : Entry(
        name = "version",
        helpKey = "sora.cmd.version.desc",
    )

    override suspend fun run() {
        sender.sendMessage("Sorapointa v$VERSION-$BUILD_BRANCH+$COMMIT_HASH")
    }
}
