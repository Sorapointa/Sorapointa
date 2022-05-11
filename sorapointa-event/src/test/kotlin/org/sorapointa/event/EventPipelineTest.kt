package org.sorapointa.event

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventPipelineTest {

    class TestEvent1 : AbstractEvent()
    class TestEvent2 : AbstractEvent()
    class TestEvent3 : AbstractEvent(), CancelableEvent

    private val list = listOf<Event>(TestEvent1(), TestEvent2(), TestEvent3(), TestEvent1(), TestEvent2(), TestEvent3())

    @BeforeAll
    fun init(): Unit = runBlocking {
        EventManager.init()
        EventManagerConfig.init()
//        EventManagerConfig.data = EventManagerConfig.Data(300L, 300L)
    }

    @Test
    fun `listener register test`() = runBlocking {
        EventManager.registerEventListener(EventPriority.LOW) {}
        EventManager.registerEventListener {}
    }

    @Test
    fun `event intercept test`() = runBlocking {
        EventManager.registerEventListener(EventPriority.LOW) {
            error("unreachable code")
        }
        EventManager.registerBlockEventListener {
            it.intercept()
        }

        list.forEach { EventManager.broadcastEvent(it) }
    }

    @Test
    fun `event cancel test`() = runBlocking {

        EventManager.registerBlockEventListener {
            if (it is CancelableEvent) {
                it.cancel()
            }
        }

        list.forEach {
            val status = EventManager.broadcastEvent(it)
            if (it is CancelableEvent) {
                assertEquals(true, status)
            }
        }
    }

    @Test
    fun `high volume test`(): Unit = runBlocking {

        EventManager.registerEventListener(EventPriority.LOW) {
            error("unreachable code")
        }
        EventManager.registerBlockEventListener {
            it.intercept()
        }

        EventManager.registerBlockEventListener(EventPriority.HIGHEST) {
            if (it is CancelableEvent) {
                it.cancel()
            }
        }

        (0..100).toList().map {
            launch {
                repeat(10) {
                    val event = TestEvent3()
                    val status = EventManager.broadcastEvent(event)
                    assertEquals(true, status)
                }
            }
        }.joinAll()
    }

    @Test
    fun `exception test`(): Unit = runBlocking {

        val parentJob = Job()
        val parentScope = CoroutineScope(CoroutineName("TestParentScope")) +
            CoroutineExceptionHandler { _, e ->
                println("Caught Exception on Test Parent Scope: ${e.stackTraceToString()}")
            } + parentJob

        EventManager.init(parentScope.coroutineContext)

        var runCount by atomic(0)

        EventManager.registerEventListener {
            delay(100)
            throw IllegalStateException()
        }

        EventManager.registerEventListener(EventPriority.LOWEST) {
            delay(500)
            runCount++
        }

        EventManager.registerBlockEventListener {
            delay(200)
            throw NullPointerException()
        }

        EventManager.registerBlockEventListener(EventPriority.LOWEST) {
            delay(500)
            runCount++
        }

        parentScope.launch {
            delay(500)
            runCount++
        }

        EventManager.broadcastEvent(TestEvent1())

        assertEquals(3, runCount)

        EventManager.initAllListeners()

        delay(300)
    }
}
