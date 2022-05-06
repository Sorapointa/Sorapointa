package org.sorapointa.event

import kotlinx.atomicfu.atomic

/**
 * Event Interface, if you want to realize your own events,
 * please inherit `AbstractEvent` or `AbstractCancellableEvent`,
 * **DON'T implement** this interface, it's sealed interface.
 *
 * @property [isIntercepted], whether this event has been intercepted
 */
sealed interface Event {

    val isIntercepted: Boolean

    /**
     * Intercept and stop this event broadcasting
     * It could intercept events broadcast to lower priority event listener.
     *
     * @see [EventManager.broadcastEvent]
     * @see [EventPriority]
     */
    fun intercept()

}

/**
 * Cancelable Event Interface, if you want to realize your own cancelable events,
 * please inherit `AbstractCancellableEvent`,
 * **DON'T implement** this interface, it's sealed interface.
 *
 * @property [isCancelled], whether this event has been cancelled
 */
sealed interface CancelableEvent : Event {

    val isCancelled: Boolean

    /**
     * Cancel this event and the state of event
     * will return to the call site of `broadcastEvent()` method.
     *
     * @see [EventManager.broadcastEvent]
     */
    fun cancel()

}

/**
 * `AbstractEvent` includes interception method,
 * which could intercept events broadcast to lower priority event listener.
 *
 * @property [isIntercepted], whether this event has been intercepted
 *
 * @see [EventManager.broadcastEvent]
 * @see [EventPriority]
 */
abstract class AbstractEvent : Event {

    override val isIntercepted: Boolean
        get() = _isIntercepted

    private var _isIntercepted by atomic(false)


    /**
     * Intercept and stop this event broadcasting
     * It could intercept events broadcast to lower priority event listener.
     *
     * @see [EventManager.broadcastEvent]
     * @see [EventPriority]
     */
    final override fun intercept() {
        _isIntercepted = true
    }

}

/**
 * `AbstractCancelableEvent` includes interception and cancellation method,
 * which could intercept the event broadcast to lower priority event listener,
 * and also could cancel the event, the final cancellation result
 * will return to the call site of `broadcastEvent()` method.
 *
 * @property [isIntercepted], whether this event has been intercepted
 *
 * @see [EventManager.broadcastEvent]
 * @see [EventPriority]
 */
abstract class AbstractCancelableEvent : AbstractEvent(), CancelableEvent {

    override val isCancelled: Boolean
        get() = _isCancelled

    private var _isCancelled by atomic(false)

    /**
     * Cancel this event and the state of event
     * will return to the call site of `broadcastEvent()` method.
     *
     * @see [EventManager.broadcastEvent]
     */
    final override fun cancel() {
        _isCancelled = true
    }

}

/**
 * Event broadcast priority
 * Decreasing priority from left to right
 * Same priority event listeners would be called in parallel.
 *
 * @see [EventManager.broadcastEvent]
 */
enum class EventPriority {
    HIGHEST, HIGH, NORMAL, LOW, LOWEST
}

