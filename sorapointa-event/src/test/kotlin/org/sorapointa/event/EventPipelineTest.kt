package org.sorapointa.event

import kotlinx.coroutines.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventPipelineTest {

    class TestEvent1: AbstractEvent()
    class TestEvent2: AbstractEvent()
    class TestEvent3: AbstractCancelableEvent()

    private val list = listOf<Event>(TestEvent1(), TestEvent2(), TestEvent3(), TestEvent1(), TestEvent2(), TestEvent3())


    @BeforeAll
    fun init(): Unit = runBlocking {
        EventManager.init()
        EventManagerConfig.reload()
        EventManagerConfig.data = EventManagerConfig.Data(300L)
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

}
