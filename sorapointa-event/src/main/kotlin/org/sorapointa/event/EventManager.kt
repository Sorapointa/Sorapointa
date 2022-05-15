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
import org.sorapointa.utils.ModuleScope
import org.sorapointa.utils.configDirectory
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

/**
 * Must call `init()` before any registration and `broadcastEvent(event)`
 *
 * @see [EventManager.init]
 */
object EventManager {

    private var eventScope = ModuleScope(logger, "EventManager")

    private val registeredListener = ConcurrentLinkedQueue<PriorityEntry>()
    private val registeredBlockListener = ConcurrentLinkedQueue<PriorityBlockEntry>()

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

        eventScope.launch {
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
            eventScope.launch {
                channels.forEach { channel ->
                    channel.send(event)
                }
            }
            val blockListeners = registeredBlockListener.first { it.priority == priority }.listeners
            if (blockListeners.size > 0) {
                withTimeout(waitingAllBlockListenersTimeout) {
                    blockListeners.asFlow().map { listener ->
                        eventScope.launch {
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
     * Initialize the queue of listener in priority order
     * and coroutine context for structured concurrency
     *
     * This method **IS NOT** thread-safe
     */
    fun init(parentContext: CoroutineContext = EmptyCoroutineContext) {
        eventScope = ModuleScope(logger, "EventManager", parentContext)
        initAllListeners()
    }

    /**
     * Initialize the queue of listener in priority order
     *
     * This method **IS NOT** thread-safe
     */
    fun initAllListeners() {
        registeredListener.clear()
        registeredBlockListener.clear()
        EventPriority.values().forEach {
            registeredListener.add(PriorityEntry(it, ConcurrentLinkedQueue()))
            registeredBlockListener.add(PriorityBlockEntry(it, ConcurrentLinkedQueue()))
        }
    }

    fun cancelAll() {
        eventScope.parentJob.cancel()
    }

    private fun getInitChannel() = Channel<Event>(64)
}

/**
 * Broadcast event in a quick way
 * @param ifNotCancel lambda block with the action if broadcasted event has **NOT** been cancelled
 */
suspend inline fun <T : Event> T.broadcastEvent(ifNotCancel: (T) -> Unit) {
    val isCancelled = EventManager.broadcastEvent(this)
    if (!isCancelled) ifNotCancel(this)
}

/**
 * Broadcast event in a quick way
 * @param ifCancelled lambda block with the action if broadcasted event has been cancelled
 */
suspend inline fun <T : Event> T.broadcastEventIfCancelled(ifCancelled: (T) -> Unit) {
    val isCancelled = EventManager.broadcastEvent(this)
    if (isCancelled) ifCancelled(this)
}

/**
 * Broadcast event in a quick way
 * @param ifNotCancel lambda block with the action if broadcasted event has **NOT** been cancelled
 */
suspend inline fun <T : Event> T.broadcastEvent(
    ifNotCancel: (T) -> Unit,
    ifCancelled: (T) -> Unit
) {
    val isCancelled = EventManager.broadcastEvent(this)
    if (!isCancelled) ifNotCancel(this) else ifCancelled(this)
}

/**
 * Broadcast event in quick way
 */
suspend inline fun <T : Event> T.broadcast() {
    EventManager.broadcastEvent(this)
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
