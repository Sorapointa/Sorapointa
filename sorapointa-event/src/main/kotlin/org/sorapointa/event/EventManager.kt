package org.sorapointa.event

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import mu.KotlinLogging
import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.utils.configDirectory
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

/**
 * Must call `init()` before any registration and `broadcastEvent(event)`
 *
 * @see [EventManager.init]
 */
object EventManager {

    private val parentJob = SupervisorJob()
    private val eventExceptionHandler =
        CoroutineExceptionHandler { _, e -> logger.error(e) { "Caught Exception on EventManager" } }
    private val eventContext = eventExceptionHandler + Dispatchers.Default + CoroutineName("EventManager") + parentJob
    private var eventScope = CoroutineScope(eventContext)

    private val registeredListener = ConcurrentLinkedQueue<PriorityEntry>()
    private val registeredBlockListener = ConcurrentLinkedQueue<PriorityBlockEntry>()
//    private val destinationChannel = ConcurrentHashMap<EventPriority, Channel<Event>>()

//    private val listeners = ConcurrentLinkedQueue<Job>()

    private val blockListenerTimeout
        get() = EventManagerConfig.data.blockListenerTimeout

    private val waitingAllBlockListenersTimeout
        get() = EventManagerConfig.data.waitingAllBlockListenersTimeout

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

        eventScope.launch(eventContext) {
            channel.receiveAsFlow().collect {
                listener(it)
            }
        }.invokeOnCompletion {
            this.registeredListener.first { it.priority == priority }.channels.remove(channel)
        }
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
        registeredBlockListener.first { it.priority == priority }.listeners.add(listener)
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

    suspend fun broadcastEvent(event: Event): Boolean {
        var isCancelled by atomic(false)
        var isIntercepted by atomic(false)
        val eventName = event::class.simpleName
        logger.debug { "Try to broadcast event $eventName" }
        registeredListener.forEach { (priority, channels) ->
            eventScope.launch(eventContext) {
                channels.forEach { channel ->
                    channel.send(event)
                }
            }
            val blockListeners = registeredBlockListener.first { it.priority == priority }.listeners
            if (blockListeners.size > 0) {
                withTimeout(waitingAllBlockListenersTimeout) {
                    blockListeners.asFlow().map { listener ->
                        eventScope.launch(eventContext) {
                            withTimeout(blockListenerTimeout) {
                                listener(event)
                            }
                            isIntercepted = event.isIntercepted
                            if (event is CancelableEvent) isCancelled = event.isCancelled
                        }
                    }.collect {
                        it.join()
                    }
                }
//                blockListeners.forEach { channel ->
//                    channel.send(event)
//                }
//                var eventReceivedCount by atomic(0)
//                val destination = destinationChannel[priority]!!
//                val closeFlow = {
//                    destination.close()
//                    destinationChannel[priority] = getInitChannel()
//                }
//                eventScope.launch {
//                    while (!destination.isClosedForSend && isActive) {
//                        if (eventReceivedCount >= blockListeners.size) {
//                            closeFlow()
//                        }
//                    }
//                }.invokeOnCompletion {
//                    if (eventReceivedCount < blockListeners.size) {
//                        closeFlow()
//                        logger.debug(it) { "Destination doesn't receive expected count of events, force closed flow." }
//                    }
//                }
//                withTimeout(DESTINATION_FLOW_TIMEOUT) {
//                    destination.receiveAsFlow().collect {
//                        isCancelled = isCancelled || (it is CancelableEvent && it.isCancelled)
//                        if (it.isIntercepted) {
//                            isIntercepted = true
//                        }
//                        eventReceivedCount++
//                    }
//                }
            }
            if (isIntercepted) {
                logger.debug { "Event $eventName has been intercepted" }
                return isCancelled
            }
        }
        logger.debug { "Broadcasted event $eventName, cancel state: $isCancelled" }
        return isCancelled
    }

    /**
     * Initialize the queue of listener in priority order, and destination channel
     */
    fun init(parentScope: CoroutineScope = eventScope) {
        eventScope = parentScope
        initAllListeners()
    }

    fun initAllListeners() {
        registeredListener.clear()
        registeredBlockListener.clear()
        EventPriority.values().forEach {
            registeredListener.add(PriorityEntry(it, ConcurrentLinkedQueue()))
            registeredBlockListener.add(PriorityBlockEntry(it, ConcurrentLinkedQueue()))
        }
    }

    fun cancelAll() {
        parentJob.cancel()
    }

    private fun getInitChannel() = Channel<Event>(64)
}

object EventManagerConfig : DataFilePersist<EventManagerConfig.Data>(
    File(configDirectory, "eventManagerConfig.json"), Data()
) {

    @kotlinx.serialization.Serializable
    data class Data(
        val blockListenerTimeout: Long = 1000 * 30L,
        val waitingAllBlockListenersTimeout: Long = 3 * blockListenerTimeout
    )
}

internal data class PriorityEntry(
    val priority: EventPriority,
    val channels: ConcurrentLinkedQueue<Channel<Event>>
)

internal data class PriorityBlockEntry(
    val priority: EventPriority,
    val listeners: ConcurrentLinkedQueue<suspend (Event) -> Unit>
)
