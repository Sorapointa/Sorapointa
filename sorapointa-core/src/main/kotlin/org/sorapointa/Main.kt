package org.sorapointa

import io.ktor.server.application.*
import kotlinx.coroutines.*
import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.core.CommandResult
import moe.sdl.yac.parameters.groups.OptionGroup
import moe.sdl.yac.parameters.groups.defaultByName
import moe.sdl.yac.parameters.groups.groupSwitch
import moe.sdl.yac.parameters.options.*
import mu.KotlinLogging
import org.jline.reader.EndOfFileException
import org.jline.reader.UserInterruptException
import org.sorapointa.SorapointaMain.Mode.*
import org.sorapointa.command.CommandManager
import org.sorapointa.command.ConsoleCommandSender
import org.sorapointa.command.defaults.defaultsCommand
import org.sorapointa.config.*
import org.sorapointa.console.Console
import org.sorapointa.console.setupConsoleClient
import org.sorapointa.console.setupWebConsoleServer
import org.sorapointa.data.provider.DatabaseManager
import org.sorapointa.dataloader.ResourceHolder
import org.sorapointa.event.EventManager
import org.sorapointa.utils.*
import java.io.File
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class SorapointaMain : CliktCommand(name = "sorapointa") {
    private val workingDirectory by option("-D", "--working-directory", help = "Set working directory")
        .convert { File(it) }
        .check("File must be directory") { (it.exists() && it.isDirectory) || (!it.exists()) }

    private val noOut by option("-N", "--no-out", help = "stdout and stderr will be disable")
        .flag(default = false)

    private val noRedirect by option("-R", "--no-redirect", help = "Whether redirect to JLine's printAbove")
        .flag(default = false)

    private sealed class Mode : OptionGroup() {
        class Server : Mode()
        class Client : Mode() {
            val username by option("--username", "--usr", "-u").required()
            val password by option("--password", "--pwd", "-p").default("")
            val wssUrl by option("--wss-url", "--url", "-I").default("wss://localhost:443/webconsole")
        }

        class Mixed : Mode()

        class Local : Mode()
    }

    private val mode by option().groupSwitch(
        "--server" to Server(),
        "--client" to Client(),
        "--local" to Local(),
        "--mixed" to Mixed(),
    ).defaultByName("--local")

    override suspend fun run(): Unit = scope.launch {
        setupShutdownHook()

        logger.info { "Version: $VERSION-$BUILD_BRANCH+$COMMIT_HASH" }

        workingDirectory?.let { System.setProperty("user.dir", it.absPath) }
        logger.info { "Sorapointa is working in $globalWorkDirectory" }

        when (val m = mode) {
            is Server -> {
                val server = setupServer {
                    it.setupWebConsoleServer()
                }
                server.join()
            }
            is Mixed -> {
                val server = setupServer {
                    it.setupWebConsoleServer()
                }
                setupLocalConsole()
                server.join()
            }
            is Local -> {
                val server = setupServer()
                setupLocalConsole()
                server.join()
            }
            is Client -> {
                redirectPrint()
                setupConsoleClient(m.username, m.password, m.wssUrl)
            }
        }
    }.join()

    private fun setupShutdownHook() {
        addShutdownHook {
            closeAll()
            println("\nExiting Sorapointa...")
            Console.redirectToNull()
        }
    }

    private fun setupServer(config: (Application) -> Unit = {}) = scope.launch {
        loadLanguages()
        setupRegisteredConfigs().join()

        setupDefaultsCommand()
        measureTimeMillis {
            Console.setupCompletion()
        }.also {
            logger.debug { "Costed ${it}ms to initialize command completion" }
        }

        EventManager.init(scope.coroutineContext)

        val databaseInitJob = setupDatabase()
        databaseInitJob.join()

        ResourceHolder.findAndRegister()
        ResourceHolder.loadAll()

        Sorapointa.init(this, scope.coroutineContext, config)
    }

    private fun setupLocalConsole() = scope.launch {
        val consoleSender = ConsoleCommandSender()
        Console.initReader()
        redirectPrint()
        while (isActive) {
            try {
                CommandManager.invokeCommand(consoleSender, Console.readln()).join()
            } catch (e: UserInterruptException) { // Ctrl + C
                println("<Interrupted> use 'quit' command to exit process")
            } catch (e: EndOfFileException) { // Ctrl + D
                exitProcess(0)
            }
        }
    }

    private fun setupRegisteredConfigs(): Job =
        scope.launch {
            logger.info { "Loading Sorapointa configs..." }
            registeredConfig.map {
                launch {
                    it.init()
                    it.save()
                }
            }.joinAll()
        }

    private fun setupDatabase(): Job =
        scope.launch {
            logger.info { "Loading Sorapointa database..." }
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
            logger.info { "Loading languages..." }
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
