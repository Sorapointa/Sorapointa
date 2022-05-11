# Event Module

## Included Config

Following config must be load at program start, like `EventManagerConfig.reload()`:

- `EventManagerConfig`

## Initialize

- `EventManager.init()` should be called at program start to initialize the queue of listener in priority order, and destination channel

## Usage

### Register Listener

All registered listeners will be called **in parallel**.

- Listen to all type of events 

```kotlin
EventManager.registerEventListener { event ->
    // do something..
}

// --- Method Signature ---

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
)
```

- Listen to specific type of events

```kotlin
EventManager.registerListener<SomeEvent> { event ->
    // do something..
}

// --- Method Signature ---

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
)
```

### Register Block Listener

All registered listeners will be called **in serial**.

- Listen to all type of events

```kotlin
EventManager.registerBlockEventListener { event ->
    // do something..
}

// --- Method Signature ---

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
)
```

- Listen to specific type of events

```kotlin
EventManager.registerBlockListener<SomeEvent> { event ->
    // do something..
}

// --- Method Signature ---

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
)
```

### Get Event Flow

```kotlin

EventManager.getEventFlow()
    .collect { event -> 
        // do something...
    }

// --- Method Signature ---

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
): Flow<Event>
```

### Broadcast Event

```kotlin
val isCancelled = EventManager.broadcastEvent(someEvent)
if (!isCancelled) {
    // do something...
} else {
    // do something else...
}

// --- Quick Way ---

someEvent.broadcast() // if `someEvent` is not `Cancellable`

someEvent.broadcastEvent { event ->
    // if this event has NOT been cancelled
}

someEvent.broadcastEventIfCancelled { event ->
  // if this event has been cancelled
}

someEvent.broadcastEvent(
  { event ->
    // if this event has been cancelled
  },
  { event ->
  // if this event has NOT been cancelled 
  }
)

// --- Method Signature ---

/**
 * Broadcast event, and return the cancel state of this event
 *
 * @param event, the event will be broadcasted
 * @return [Boolean], that represents the cancel state of this event
 */
suspend fun broadcastEvent(event: Event): Boolean
```

## Broadcast Path

![Snipaste_2022-05-06_15-46-22](https://user-images.githubusercontent.com/25319400/167162147-a9302a06-8aa6-4d60-a568-147dcb9c7586.png)

