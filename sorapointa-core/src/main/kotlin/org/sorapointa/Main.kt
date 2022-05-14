package org.sorapointa

import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.core.CommandResult
import moe.sdl.yac.parameters.options.check
import moe.sdl.yac.parameters.options.convert
import moe.sdl.yac.parameters.options.flag
import moe.sdl.yac.parameters.options.option
import mu.KotlinLogging
import org.jline.reader.EndOfFileException
import org.jline.reader.UserInterruptException
import org.sorapointa.command.CommandManager
import org.sorapointa.command.ConsoleCommandSender
import org.sorapointa.command.defaults.defaultsCommand
import org.sorapointa.config.BUILD_BRANCH
import org.sorapointa.config.COMMIT_HASH
import org.sorapointa.config.VERSION
import org.sorapointa.config.registeredConfig
import org.sorapointa.console.Console
import org.sorapointa.console.JLineRedirector
import org.sorapointa.utils.*
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

class Sorapointa : CliktCommand(name = "sorapointa") {
    private val scope = ParentScope(logger, "Sorapointa")

    private val workingDirectory by option("-D", "--working-directory", help = "Set working directory")
        .convert { File(it) }
        .check("File must be directory") { (it.exists() && it.isDirectory) || (!it.exists()) }

    private val noOut by option("-N", "--no-out", help = "stdout and stderr will be disable")
        .flag(default = false)

    private val noRedirect by option("-R", "--no-redirect", help = "Print(ln) won't redirect to JLine's printAbove")
        .flag(default = false)

    override suspend fun run(): Unit = scope.launch {
        redirectPrint()

        addShutdownHook {
            Console
            println("\nExiting sorapointa...")
        }

        logger.info { "Version: $VERSION-$BUILD_BRANCH-$COMMIT_HASH" }

        workingDirectory?.let { System.setProperty("user.dir", it.absPath) }
        logger.info { "Sorapointa is working in $globalWorkDirectory" }

        logger.info { "Loading sorapointa configs..." }
        setupRegisteredConfigs()

        logger.info { "Loading languages..." }
        loadLanguages()

        setupDefaultsCommand()

        launch {
            while (isActive) {
                try {
                    CommandManager.invokeCommand(ConsoleCommandSender(), Console.readln())
                } catch (e: UserInterruptException) { // Ctrl + C
                    println("<Interrupted> use 'quit' command to exit process")
                } catch (e: EndOfFileException) { // Ctrl + D
                    exitProcess(0)
                }
            }
        }
    }.join()

    private fun setupRegisteredConfigs(): Job =
        scope.launch {
            registeredConfig.map {
                launch { it.init() }
            }.joinAll()
        }

    private fun setupDefaultsCommand() {
        CommandManager.registerCommands(defaultsCommand)
        val registered = defaultsCommand.joinToString(", ") { it.entry.name }
        logger.info { "Registered defaults command, total ${defaultsCommand.size}: $registered" }
    }

    private fun loadLanguages(): Job =
        scope.launch {
            I18nManager.registerLanguagesDirectory(languagesDirectory)
            logger.info { "Loaded languages: ${I18nManager.supportedLanguages.joinToString { it.toLanguageTag() }}" }
        }

    private fun redirectPrint() {
        when {
            noOut -> {
                val out = PrintStream(OutputStream.nullOutputStream())
                System.setOut(out)
                System.setErr(out)
            }
            !noOut && !noRedirect -> {
                System.setErr(JLineRedirector)
                System.setOut(JLineRedirector)
            }
            else -> {
                // keep origin
            }
        }
    }
}

suspend fun main(args: Array<String>) {
    when (val result = Sorapointa().main(args)) {
        is CommandResult.Success -> {
            exitProcess(0)
        }
        is CommandResult.Error -> {
            println(result.userMessage)
            exitProcess(1)
        }
    }
}
