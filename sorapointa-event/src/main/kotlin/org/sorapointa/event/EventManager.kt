package org.sorapointa.event

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import mu.KotlinLogging
import org.sorapointa.data.provider.ReadOnlyFilePersist
import org.sorapointa.utils.configDirectory
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue


private val logger = KotlinLogging.logger {}

/**
 * Must call `init()` before any registration and `broadcastEvent(event)`
 *
 * @see [EventManager.init]
 */
object EventManager {

    private val eventExceptionHandler =
        CoroutineExceptionHandler { _, e -> logger.error(e) { "Caught Exception on EventManager" } }
    private val eventScope = CoroutineScope(eventExceptionHandler) + CoroutineName("EventManager") + SupervisorJob()

    private val registeredListener = ConcurrentLinkedQueue<PriorityEntry>()
    private val registeredBlockListener = ConcurrentLinkedQueue<PriorityEntry>()
    private val destinationChannel = ConcurrentHashMap<EventPriority, Channel<Event>>()

    private val listeners = ConcurrentLinkedQueue<Job>()

    private val BLOCK_LISTENER_TIMEOUT
        get() = EventManagerConfig.data.blockListenerTimeout

    private val DESTINATION_FLOW_TIMEOUT
        get() = EventManagerConfig.data.destinationFlowTimeout

    /**
     * Get original event flow from broadcasting
     * Flow would receive the event data in parallel
     *
     * You could **NOT CANCEL** or **INTERCEPT** any event in parallel listener.
     *
     * @param priority, optional, set the priority of this listener
     * @return [Flow]
     */
    fun getEventFlow(
        priority: EventPriority = EventPriority.NORMAL,
    ): Flow<Event> {
        val channel = getInitChannel()
        registeredListener.first { it.priority == priority }.channels.add(channel)
        return channel.receiveAsFlow()
    }

    /**
     * Register a parallel listener
     * All registered listeners will be called in parallel.
     *
     * You could **NOT CANCEL** or **INTERCEPT** any event in parallel listener.
     *
     * @param priority, optional, set the priority of this listener
     * @param listener, lambda block of your listener with the all event of parameter
     */
    fun registerEventListener(
        priority: EventPriority = EventPriority.NORMAL,
        listener: suspend (Event) -> Unit
    ) {
        val channel = getInitChannel()
        registeredListener.first { it.priority == priority }.channels.add(channel)

        val listenerJob = eventScope.launch {
            channel.receiveAsFlow().collect {
                listener(it)
            }
        }
        listenerJob.invokeOnCompletion {
            this.registeredListener.first { it.priority == priority }.channels.remove(channel)
        }
        listeners.add(listenerJob)
    }

    /**
     * Register a parallel listener
     * All registered listeners will be called in parallel.
     *
     * You could NOT CANCEL or INTERCEPT any event in parallel listener.
     *
     * Generic type [T] is what event would you listen.
     *
     * @param priority, optional, set the priority of this listener
     * @param listener, lambda block of your listener with the specific event of parameter
     */
    inline fun <reified T : Event> registerListener(
        priority: EventPriority = EventPriority.NORMAL,
        crossinline listener: suspend (T) -> Unit
    ) {
        registerEventListener(priority) {
            if (it is T) {
                listener(it)
            }
        }
    }

    /**
     * Register a block listener
     * All registered block listeners will be called in serial.
     *
     * You could cancel or intercept listened event in serial listener.
     *
     * @param priority, optional, set the priority of this listener
     * @param listener, lambda block of your listener with all type of event of parameter
     */
    fun registerBlockEventListener(
        priority: EventPriority = EventPriority.NORMAL,
        listener: suspend (Event) -> Unit
    ) {
        val channel = getInitChannel()
        registeredBlockListener.first { it.priority == priority }.channels.add(channel)
        val listenerJob = eventScope.launch {
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
        listeners.add(listenerJob)
        listenerJob.invokeOnCompletion {
            this.registeredBlockListener.first { it.priority == priority }.channels.remove(channel)
        }
    }

    /**
     * Register a block listener
     * All registered block listeners will be called in serial.
     *
     * You could cancel or intercept listened event in serial listener.
     *
     * Generic type [T] is what event would you listen.
     *
     * @param priority, optional, set the priority of this listener
     * @param listener, lambda block of your listener with the specific event of parameter
     */
    inline fun <reified T : Event> registerBlockListener(
        priority: EventPriority = EventPriority.NORMAL,
        crossinline listener: suspend (T) -> Unit
    ) {
        registerBlockEventListener(priority) {
            if (it is T) {
                listener(it)
            }
        }
    }

    /**
     * Broadcast event, and return the cancel state of this event
     *
     * @param event, the event will be broadcasted
     * @return [Boolean], that represents the cancel state of this event
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun broadcastEvent(event: Event): Boolean {
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
                        while (!destination.isClosedForSend && isActive) {
                            if (eventReceivedCount >= channels.size) {
                                destination.close()
                                destinationChannel[priority] = getInitChannel()
                            }
                        }
                    }
                    withTimeout(DESTINATION_FLOW_TIMEOUT) {
                        destination.receiveAsFlow().collect {
                            isCancelled = isCancelled || (it is CancelableEvent && it.isCancelled)
                            if (it.isIntercepted) {
                                isIntercepted = true
                            }
                            eventReceivedCount++
                        }
                    }
                }
                if (isIntercepted) return isCancelled
            }
        }
        return isCancelled
    }

    /**
     * Initialize the queue of listener in priority order, and destination channel
     */
    fun init() {
        EventPriority.values().forEach {
            registeredListener.add(PriorityEntry(it, ConcurrentLinkedQueue()))
            registeredBlockListener.add(PriorityEntry(it, ConcurrentLinkedQueue()))
            destinationChannel[it] = getInitChannel()
        }
    }

    fun closeAll() {
        eventScope.cancel()
    }

    private fun getInitChannel() = Channel<Event>(64)

}

object EventManagerConfig : ReadOnlyFilePersist<EventManagerConfig.Data>(
    File(configDirectory, "eventManagerConfig.json"), Data()
) {

    @kotlinx.serialization.Serializable
    data class Data(
        val destinationFlowTimeout: Long = 1000 * 30L,
        val blockListenerTimeout: Long = 1000 * 30L
    )

}

internal data class PriorityEntry(
    val priority: EventPriority,
    val channels: ConcurrentLinkedQueue<Channel<Event>>
)
