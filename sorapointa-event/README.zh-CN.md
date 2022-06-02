# 事件模块

[English](README.zh-CN.md)

你可以查看 [EventPipelineTest](src/test/kotlin/org/sorapointa/event/EventPipelineTest.kt) 提供的例子，
与在 [EventManager](src/main/kotlin/org/sorapointa/event/EventManager.kt) 
和 [Event](src/main/kotlin/org/sorapointa/event/Event.kt) 代码中提供的详细文档

## 事件模块包括的配置

以下配置必须在程序启动时加载，如 `EventManagerConfig.reload()`:

- `EventManagerConfig`

## 初始化

- `EventManager.init(parentCoroutineContext)` 应该在程序开始时被调用，
以便按优先级顺序初始化监听器队列，并设置其父协程上下文以确保结构化并发。

## 用法

### 注册监听器

所有通过 `registerEventListener` 方法注册的监听器都会被并发平行调用，
在这种监听器中设置 `cancel` 或者 `interrupt`，是无意义的。

- 监听所有事件

```kotlin
EventManager.registerEventListener { event ->
    // do something..
}

// --- 方法签名 ---

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

- 监听特殊的一类事件

```kotlin
EventManager.registerListener<SomeEvent> { event ->
    // do something..
}

// --- 方法签名 ---

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

### 注册阻塞监听器

所有通过 `registerBlockEventListener` 方法注册的监听器都会被串行阻塞调用，
在这种监听器中设置 `cancel` 或者 `interrupt` 才是有效的。

- 监听所有事件

```kotlin
EventManager.registerBlockEventListener { event ->
    // do something..
}

// --- 方法签名 ---

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

- 监听特殊的一类事件

```kotlin
EventManager.registerBlockListener<SomeEvent> { event ->
    // do something..
}

// --- 方法签名 ---

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

### 获取事件流

```kotlin

EventManager.getEventFlow()
    .collect { event -> 
        // do something...
    }

// --- 方法签名 ---

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

### 广播事件

```kotlin
val isCancelled = EventManager.broadcastEvent(someEvent)
if (!isCancelled) {
    // do something...
} else {
    // do something else...
}

// --- 更方便的拓展方法 ---

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

// --- 方法签名 ---

/**
 * Broadcast event, and return the cancel state of this event
 *
 * @param event, the event will be broadcasted
 * @return [Boolean], that represents the cancel state of this event
 */
suspend fun broadcastEvent(event: Event): Boolean
```

## 事件的广播路径

![Snipaste_2022-05-06_15-46-22](https://user-images.githubusercontent.com/25319400/167162147-a9302a06-8aa6-4d60-a568-147dcb9c7586.png)

# 状态机模块

你可以查看 [StateControllerTest](src/test/kotlin/org/sorapointa/event/StateControllerTest.kt) 提供的例子，
与在 [StateController](src/main/kotlin/org/sorapointa/event/StateController.kt) 代码中提供的详细文档
