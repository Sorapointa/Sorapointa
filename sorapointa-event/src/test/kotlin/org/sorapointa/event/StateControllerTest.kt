package org.sorapointa.event

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.sorapointa.utils.ModuleScope
import kotlin.test.assertEquals

private val logger = KotlinLogging.logger {}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StateControllerTest {

    interface SomeClassWithState : WithState<SomeClassWithState.State> {

        fun foobar(): String

        enum class State {
            START,
            DOING,
            END
        }

    }

    interface SomeClassWithSomeDifferentState : WithState<SomeClassWithSomeDifferentState.State> {

        fun barfoo(): String

        enum class State {
            HAPPY,
            BAD,
            CRY
        }

    }

    class SomeClassWithStateImpl {

        var count by atomic(0)


        val stateController = StateController(
            scope = ModuleScope(logger, "TestScopeWithState"),
            parentStateClass = this,
            Start(), Doing(), End(),
        )

        val stateController2 = StateController(
            scope = ModuleScope(logger, "TestScopeWithState2"),
            parentStateClass = this,
            Happy(), Bad(), Cry()
        )

        fun foobar(): String {
            return stateController.getState().foobar()
        }

        fun barfoo(): String {
            return stateController2.getState().barfoo()
        }

        inner class Happy: SomeClassWithSomeDifferentState {

            override val state: SomeClassWithSomeDifferentState.State = SomeClassWithSomeDifferentState.State.HAPPY

            override fun barfoo(): String = "Happy"

        }

        inner class Bad: SomeClassWithSomeDifferentState {

            override val state: SomeClassWithSomeDifferentState.State = SomeClassWithSomeDifferentState.State.BAD

            override fun barfoo(): String = "Bad"

        }

        inner class Cry: SomeClassWithSomeDifferentState {

            override val state: SomeClassWithSomeDifferentState.State = SomeClassWithSomeDifferentState.State.CRY

            override fun barfoo(): String = "Cry"

        }


        inner class Start: SomeClassWithState {

            override val state: SomeClassWithState.State = SomeClassWithState.State.START

            override fun foobar(): String {
                return "Start!"
            }

            override suspend fun endState() {
                count++
                println("Start!")
            }

        }

        inner class Doing: SomeClassWithState {

            override val state: SomeClassWithState.State = SomeClassWithState.State.DOING

            override fun foobar(): String {
                return "Doing!"
            }

            override suspend fun startState() {
                count++
                println("Doing!")
            }

            override suspend fun endState() {
                count++
                println("Done!")
            }

        }

        inner class End: SomeClassWithState {

            override val state: SomeClassWithState.State = SomeClassWithState.State.END

            override fun foobar(): String {
                return "End!"
            }

            override suspend fun startState() {
                count++
                println("End!")
            }

        }


    }

    @Test
    fun `state controller test`() = runBlocking {

        val sc = SomeClassWithStateImpl()

        sc.stateController.observeStateChange(StateController.ListenerState.AFTER_UPDATE) { before, after ->
            println("Count: $count, before: $before, after: $after")
        }

        sc.stateController.intercept(StateController.ListenerState.BEFORE_UPDATE) {
            count >= 4
        }

        sc.stateController.block {
            throw NullPointerException()
            // throw
        }


        assertEquals("Start!", sc.foobar())
        sc.stateController.setState(SomeClassWithState.State.DOING)
        assertEquals("Doing!", sc.foobar())
        sc.stateController.setState(SomeClassWithState.State.END)
        assertEquals("End!", sc.foobar())
        sc.stateController.setState(SomeClassWithState.State.START) // Fail, will be intercepted
        assertEquals("End!", sc.foobar())

        assertEquals("Happy", sc.barfoo())
        sc.stateController2.setState(SomeClassWithSomeDifferentState.State.BAD)
        assertEquals("Bad", sc.barfoo())
        sc.stateController2.setState(SomeClassWithSomeDifferentState.State.CRY)
        assertEquals("Cry", sc.barfoo())



    }

    @Test
    fun `high volume test`() = runBlocking {

        val testCount = 1000

        val sc = SomeClassWithStateImpl()
        (1..testCount).map {
            launch {
                sc.stateController.setState(SomeClassWithState.State.START)
                assertEquals("Start!", sc.foobar())
                sc.stateController.setState(SomeClassWithState.State.DOING)
                assertEquals("Doing!", sc.foobar())
                sc.stateController.setState(SomeClassWithState.State.END)
                assertEquals("End!", sc.foobar())
            }
        }.joinAll()

        assertEquals(testCount * 4 + 1, sc.count)

        val sc2 = SomeClassWithStateImpl()
        sc2.stateController.block {
            delay((0..200L).random())
        }
        (1..testCount).map {
            launch {
                sc2.stateController.setState(SomeClassWithState.State.START)
                assertEquals("Start!", sc2.foobar())
                sc2.stateController.setState(SomeClassWithState.State.DOING)
                assertEquals("Doing!", sc2.foobar())
                sc2.stateController.setState(SomeClassWithState.State.END)
                assertEquals("End!", sc2.foobar())
            }
        }.joinAll()


    }





}
