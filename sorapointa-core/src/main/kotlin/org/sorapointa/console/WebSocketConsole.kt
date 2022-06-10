package org.sorapointa.console

import com.password4j.Password
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import org.jline.reader.EndOfFileException
import org.jline.reader.UserInterruptException
import org.sorapointa.command.CommandManager
import org.sorapointa.command.RemoteCommandSender
import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.dispatch.data.argon2Function
import org.sorapointa.utils.ModuleScope
import org.sorapointa.utils.configDirectory
import org.sorapointa.utils.encoding.RSAProvider
import org.sorapointa.utils.encoding.encodeBase64
import org.sorapointa.utils.networkJson
import java.io.File
import java.security.KeyPair
import java.security.cert.X509Certificate
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl.X509TrustManager
import kotlin.collections.set
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

@Serializable
internal sealed class WebConsolePacket

@Serializable
internal object PublicKeyReq : WebConsolePacket()

@Serializable
internal class PublicKeyResp(val publicKey: String) : WebConsolePacket()

/**
 * @param username user to login
 * @param password password encrypted by RSA [PublicKeyResp.publicKey]
 */
@Serializable
internal class VerifyReq(val username: String, val password: String) : WebConsolePacket()

@Serializable
internal class VerifyResp(val ok: Boolean) : WebConsolePacket()

@Serializable
internal class CommandReq(val command: String) : WebConsolePacket()

@Serializable
internal object CommandEndResp : WebConsolePacket()

@Serializable
internal class MessageNotify(val message: String) : WebConsolePacket()

internal object ConsoleUsers : DataFilePersist<ConsoleUsers.Data>(
    File(configDirectory, "consoleUsers.json"),
    Data()
) {
    @Serializable
    data class Data(
        var publicKey: String? = null,
        var privateKey: String? = null,
        val tokenLength: Int = 128,
        val users: MutableMap<String, String> = ConcurrentHashMap<String, String>(),
    )

    override suspend fun init() = withContext(Dispatchers.IO) {
        super.init()
        if (data.publicKey == null || data.privateKey == null) {
            genKeyPair()
        }
        save()
    }

    private suspend fun genKeyPair(): KeyPair = withContext(Dispatchers.Default) {
        val pair = RSAProvider.keyPairGenerate()
        data.publicKey = pair.public.encoded.encodeBase64()
        data.privateKey = pair.private.encoded.encodeBase64()
        pair
    }

    suspend fun getPublicKeyOrGen() =
        data.publicKey ?: genKeyPair().public.encoded.encodeBase64()

    suspend fun getPrivateKeyOrGen() =
        data.privateKey ?: genKeyPair().private.encoded.encodeBase64()

    fun addOrUpdate(username: String, password: String) {
        val user = username.lowercase()
        data.users[user] = Password.hash(password).addSalt(user).with(argon2Function).result
    }

    fun verify(username: String, password: String): Boolean {
        val user = username.lowercase()
        val encrypted = data.users[user] ?: return false
        return Password.check(password, encrypted).addSalt(user).with(argon2Function)
    }
}

internal fun Application.setupWebConsoleServer() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(networkJson)
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(60)
    }

    routing {
        webSocket("/webconsole") {
            logger.info { "WebSocket Console Connected" }

            val closedNotifyJob = launch {
                val reason = closeReason.await()
                logger.info { "Closed: $reason" }
            }

            val _verified = atomic(false)
            var verified by _verified

            val remoteSender = RemoteCommandSender(this, null)

            launch {
                delay(30_000)
                if (!verified) {
                    close(CloseReason(CloseReason.Codes.NORMAL, "Client is not verified after 30 seconds"))
                }
            }

            incoming.consumeEach { frame ->
                try {
                    if (frame !is Frame.Text) {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Only accept for Text data"))
                        logger.info { "WebSocketConsole closed because of illegal data type ${frame.frameType}" }
                        return@consumeEach
                    }
                    val pkt = runCatching {
                        val json = frame.readText()
                        logger.debug { "Received json: $json" }
                        networkJson.decodeFromString<WebConsolePacket>(json)
                    }.onFailure {
                        if (it is CancellationException) {
                            throw it
                        } else logger.info(it) { "Unexpected exception:" }
                    }.getOrNull() ?: return@consumeEach
                    when (pkt) {
                        is PublicKeyReq -> {
                            sendSerialized<WebConsolePacket>(PublicKeyResp(ConsoleUsers.getPublicKeyOrGen()))
                        }
                        is VerifyReq -> {
                            if (verified) {
                                sendSerialized<WebConsolePacket>(VerifyResp(true))
                                return@consumeEach
                            }

                            val pwd = RSAProvider.decryptWithPrivateKey(
                                ConsoleUsers.getPrivateKeyOrGen(),
                                pkt.password
                            )
                            val success = ConsoleUsers.verify(pkt.username, pwd)
                            verified = success
                            if (success) {
                                logger.debug { "Successfully verified for user '${pkt.username}'" }
                            }
                            Console.consoleUsers.add(remoteSender)
                            sendSerialized<WebConsolePacket>(VerifyResp(success))
                        }
                        is CommandReq -> {
                            if (!verified) {
                                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Not verified"))
                                return@consumeEach
                            }
                            CommandManager.invokeCommand(remoteSender, pkt.command)
                            sendSerialized<WebConsolePacket>(CommandEndResp)
                        }
                        else ->
                            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Illegal Data Class"))
                    }
                } catch (e: ClosedReceiveChannelException) {
                    logger.info(e) { "WebSocketConsole Closed by client" }
                }
            }
            closedNotifyJob.join()
        }
    }
}

private val client by lazy {
    HttpClient(CIO) {
        install(io.ktor.client.plugins.websocket.WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(networkJson)
        }
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
                    override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                }
            }
        }
    }
}

internal suspend fun setupConsoleClient(username: String, password: String, url: String) {
    val scope = ModuleScope("ConsoleClient")
    client.webSocket(url) {
        logger.info { "Try connecting remote..." }
        sendSerialized<WebConsolePacket>(PublicKeyReq)

        val closedNotifyJob = scope.launch {
            val reason = closeReason.await()
            logger.info { "Closed: $reason" }
        }

        val mutex = Mutex()

        val incomingJob = scope.launch {
            incoming.consumeEach {
                val json = (it as? Frame.Text)?.readText() ?: return@consumeEach
                logger.debug { "Received json: $json" }
                when (val pkt = networkJson.decodeFromString<WebConsolePacket>(json)) {
                    is PublicKeyResp -> {
                        sendSerialized<WebConsolePacket>(
                            VerifyReq(
                                username,
                                RSAProvider.encryptWithPublicKey(pkt.publicKey, password)
                            )
                        )
                    }
                    is VerifyResp -> {
                        if (!pkt.ok) error("Server denied connection, maybe username or password error")
                        logger.info { "Successfully connected to remote!" }
                    }
                    is MessageNotify -> println(pkt.message)
                    is CommandEndResp -> mutex.unlock()
                    else -> {
                        // do nothing
                    }
                }
            }
        }

        val inputJob = scope.launch {
            while (isActive) {
                try {
                    mutex.lock()
                    sendSerialized<WebConsolePacket>(CommandReq(Console.readln()))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: UserInterruptException) {
                    println("<Interrupted> use Ctrl + D to exit client")
                } catch (e: EndOfFileException) {
                    exitProcess(0)
                }
            }
        }

        listOf(closedNotifyJob, incomingJob, inputJob).joinAll()
    }
}
