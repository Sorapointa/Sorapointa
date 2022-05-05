package org.sorapointa.event

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFails

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventPipelineTest {

    class TestEvent1: AbstractEvent()
    class TestEvent2: AbstractEvent()
    class TestEvent3: AbstractCancelableEvent()

    val list = listOf<Event>(TestEvent1(), TestEvent2(), TestEvent3(), TestEvent1(), TestEvent2(), TestEvent3())


    @BeforeAll
    fun init(): Unit = runBlocking {
        EventManager.init()
        EventManagerConfig.reload()
        EventManagerConfig.data = EventManagerConfig.Data(300L)
    }

    @AfterEach
    fun close(): Unit {

    }

    @Test
    fun `listener register test`() = runBlocking {
        EventManager.registerListener(EventPriority.LOW) {}
        EventManager.registerListener {}
    }

    @Test
    fun `event intercept test`() = runBlocking {
        EventManager.registerListener(EventPriority.LOW) {
            error("unreachable code")
        }
        EventManager.registerBlockListener {
            it.intercept()
        }

        list.forEach { EventManager.callEvent(it) }

    }

    @Test
    fun `event cancel test`() = runBlocking {

        EventManager.registerBlockListener {
            if (it is CancelableEvent) {
                it.cancel()
            }
        }

        list.forEach {
            val status = EventManager.callEvent(it)
            if (it is CancelableEvent) {
                assertEquals(true, status)
            }
        }
    }



}
