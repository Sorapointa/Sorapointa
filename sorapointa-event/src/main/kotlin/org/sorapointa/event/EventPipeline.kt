package org.sorapointa.event

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import mu.KotlinLogging
import org.jetbrains.annotations.TestOnly
import org.sorapointa.data.provider.ReadOnlyFilePersist
import org.sorapointa.utils.configDirectory
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext


private val logger = KotlinLogging.logger {}

/**
 * Must call `init()` before any registration and `callEvent`
 */
object EventManager {

    private val eventExceptionHandler =
        CoroutineExceptionHandler { _, e -> logger.error(e) { "Caught Exception on EventManager" } }
    private val eventScope = CoroutineScope(eventExceptionHandler) + CoroutineName("EventManager")

    private val registeredListener = ConcurrentLinkedQueue<PriorityEntry>()
    private val registeredBlockListener = ConcurrentLinkedQueue<PriorityEntry>()
    private val destinationChannel = ConcurrentHashMap<EventPriority, Channel<Event>>()

    private val BLOCK_LISTENER_TIMEOUT = EventManagerConfig.data.blockListenerTimeout


    fun init() {
        registeredListener.clear()
        registeredBlockListener.clear()
        EventPriority.values().forEach {
            registeredListener.add(PriorityEntry(it, ConcurrentLinkedQueue()))
            registeredBlockListener.add(PriorityEntry(it, ConcurrentLinkedQueue()))
            destinationChannel[it] = getInitChannel()
        }
    }

    fun closeAll() {
        eventScope.cancel()
    }

    fun registerListener(
        priority: EventPriority = EventPriority.NORMAL,
        channel: Channel<Event> = getInitChannel()
    ): Flow<Event> {
        registeredListener.first { it.priority == priority }.channels.add(channel)
        return channel.receiveAsFlow()
    }

    fun registerListener(
        priority: EventPriority = EventPriority.NORMAL,
        channel: Channel<Event> = getInitChannel(),
        listener: suspend (Event) -> Unit
    ) {
        registeredListener.first { it.priority == priority }.channels.add(channel)
        eventScope.launch {
            channel.receiveAsFlow().collect {
                listener(it)
            }
        }
    }


    fun registerBlockListener(
        priority: EventPriority = EventPriority.NORMAL,
        channel: Channel<Event> = getInitChannel(),
        listener: suspend (Event) -> Unit
    ) {
        registeredBlockListener.first { it.priority == priority }.channels.add(channel)
        eventScope.launch {
            channel.receiveAsFlow().collect {
                val destination = destinationChannel[priority]!!
                try {
                    withTimeout(BLOCK_LISTENER_TIMEOUT) {
                        listener(it)
                        destination.send(it)
                    }
                } catch (e: TimeoutCancellationException) {
                    destination.send(it)
                    logger.error(e) { "Event ${it::class.qualifiedName} has timed out after $BLOCK_LISTENER_TIMEOUT ms" }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun callEvent(event: Event): Boolean {
        var isCancelled by atomic(false)
        var isIntercepted by atomic(false)
        registeredListener.forEach { (priority, channels) ->
            eventScope.launch {
                channels.forEach { channel ->
                    channel.send(event)
                }
            }
            registeredBlockListener.filter { it.priority == priority }.forEach { (priority, channels) ->
                if (channels.size > 0) {
                    channels.forEach { channel ->
                        channel.send(event)
                    }
                    var eventReceivedCount by atomic(0)
                    val destination = destinationChannel[priority]!!
                    eventScope.launch {
                        while (!destination.isClosedForSend) {
                            println("eventReceived: $eventReceivedCount")
                            if (eventReceivedCount >= channels.size) {
                                destination.close()
                                destinationChannel[priority] = getInitChannel()
                            }
                        }
                    }
                    destination.receiveAsFlow().collect {
                        isCancelled = isCancelled || (it is CancelableEvent && it.isCancelled)
                        if (it.isIntercepted) {
                            isIntercepted = true
                        }
                        eventReceivedCount++
                    }
                }
                if (isIntercepted) return isCancelled
            }
        }
        return isCancelled
    }

    private fun getInitChannel() = Channel<Event>(
        capacity = Channel.BUFFERED,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

}

object EventManagerConfig: ReadOnlyFilePersist<EventManagerConfig.Data>(
    File(configDirectory, "eventManagerConfig.json"), Data()
) {

    @kotlinx.serialization.Serializable
    data class Data(
        val blockListenerTimeout: Long = 1000 * 30L
    )

}

data class PriorityEntry(
    val priority: EventPriority,
    val channels: ConcurrentLinkedQueue<Channel<Event>>
)

enum class EventPriority {
    HIGHEST, HIGH, NORMAL, LOW, LOWEST
}


abstract class AbstractEvent : Event {

    override val isIntercepted: Boolean
        get() = _isIntercepted

    private var _isIntercepted by atomic(false)


    final override fun intercept() {
        _isIntercepted = true
    }

}

abstract class AbstractCancelableEvent : AbstractEvent(), CancelableEvent {

    override val isCancelled: Boolean
        get() = _isCancelled

    private var _isCancelled by atomic(false)


    final override fun cancel() {
        _isCancelled = true
    }

}


sealed interface Event {

    val isIntercepted: Boolean

    fun intercept()


}


sealed interface CancelableEvent : Event {

    val isCancelled: Boolean

    fun cancel()

}


/*

(Cancel)
Data -> Pipe -> Pipe -> Pipe -> Return
     -> (Pipe) Return | Queue
     -> (Pipe) Return | Queue



 */
