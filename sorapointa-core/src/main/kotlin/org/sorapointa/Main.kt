package org.sorapointa

import kotlinx.coroutines.*
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
import org.sorapointa.config.*
import org.sorapointa.console.Console
import org.sorapointa.data.provider.DatabaseManager
import org.sorapointa.event.EventManager
import org.sorapointa.utils.*
import java.io.File
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

class SorapointaMain : CliktCommand(name = "sorapointa") {

    private val workingDirectory by option("-D", "--working-directory", help = "Set working directory")
        .convert { File(it) }
        .check("File must be directory") { (it.exists() && it.isDirectory) || (!it.exists()) }

    private val noOut by option("-N", "--no-out", help = "stdout and stderr will be disable")
        .flag(default = false)

    private val noRedirect by option("-R", "--no-redirect", help = "Whether redirect to JLine's printAbove")
        .flag(default = false)

    override suspend fun run(): Unit = scope.launch {
        redirectPrint()

        addShutdownHook {
            closeAll()
            println("\nExiting Sorapointa...")
            Console.redirectToNull()
        }

        logger.info { "Version: $VERSION-$BUILD_BRANCH-$COMMIT_HASH" }

        workingDirectory?.let { System.setProperty("user.dir", it.absPath) }
        logger.info { "Sorapointa is working in $globalWorkDirectory" }

        logger.info { "Loading languages..." }
        loadLanguages()

        logger.info { "Loading Sorapointa configs..." }
        setupRegisteredConfigs().join()

        setupDefaultsCommand()
        EventManager.init(scope.coroutineContext)

        logger.info { "Loading Sorapointa database..." }
        val databaseInitJob = setupDatabase()

        databaseInitJob.join()

        Sorapointa.init(scope.coroutineContext)

        // setup console command input
        launch {
            while (isActive) {
                try {
                    CommandManager.invokeCommand(ConsoleCommandSender(), Console.readln()).join()
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

    private fun setupDatabase(): Job =
        scope.launch {
            DatabaseManager.loadDatabase()
            DatabaseManager.loadTables(registeredDatabaseTable)
        }

    private fun setupDefaultsCommand() {
        CommandManager.registerCommands(defaultsCommand)
        val registered = defaultsCommand.joinToString(", ") { it.entry.name }
        logger.info { "Registered defaults command, total ${defaultsCommand.size}: $registered" }
    }

    private fun loadLanguages(): Job =
        scope.launch {
            extractLanguages(SorapointaMain::class)
            I18nManager.registerLanguagesDirectory(languagesDirectory)
            logger.info { "Loaded languages: ${I18nManager.supportedLanguages.joinToString { it.toLanguageTag() }}" }
        }

    private fun redirectPrint() {
        when {
            noOut -> Console.redirectToNull()
            !noOut && !noRedirect -> Console.redirectToJLine()
            else -> {
                // keep origin
            }
        }
    }

    companion object {

        private val scope = ModuleScope("SorapointaRootScope")

        internal fun closeAll() {
            scope.dispose()
            scope.cancel()
        }
    }
}

suspend fun main(args: Array<String>) {
    when (val result = SorapointaMain().main(args)) {
        is CommandResult.Success -> {
            exitProcess(0)
        }
        is CommandResult.Error -> {
            println(result.userMessage)
            exitProcess(1)
        }
    }
}
