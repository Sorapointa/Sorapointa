@file:Suppress("unused")

package org.sorapointa.event

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.sorapointa.event.StateController.ListenerState
import org.sorapointa.utils.ModuleScope
import org.sorapointa.utils.SorapointaInternal
import java.util.concurrent.ConcurrentHashMap

/**
 * Interface for different state class
 *
 * You should implement it with your custom state interface
 *
 * ```
 * interface SomeClassWithState : WithState<SomeClassWithState.State> {
 *
 *  // This is a method you wanted to be differential implementation with different states
 *  fun foobar(): String
 *
 *  // An enum includes all your states
 *  enum class State {
 *      START,
 *      DOING,
 *      END
 *  }
 * }
 * ```
 * @property state indicated state of this class
 * @see StateController
 */
interface WithState<out T : Enum<*>> {

    val state: T

    /**
     * This method would be called when this state starts
     *
     * @see StateController.setState
     */
    suspend fun startState() {
    }

    /**
     * This method would be called when this state ends
     *
     * @see StateController.setState
     */
    suspend fun endState() {
    }
}

/**
 * A simple state controller
 *
 * Generic [S] is an enum included all states of this controller,
 * [I] is your custom implementation of [WithState] interface,
 * [C] is your parent state that would
 * be a receiver in state observer, and interceptor lambda.
 *
 * You should call [StateController.init] to make sure
 * your first state has been correctly start with [WithState.startState].
 *
 * You should use [StateController] like following example:
 *
 * ```
 * class SomeClassWithStateImpl {
 *
 *  val count = atomic(0)
 *
 *  val stateController = InitStateController(
 *      scope = ModuleScope("TestScopeWithState"),
 *      parentStateClass = this,
 *      Start(), Doing(), End(),
 *  )
 *
 *
 *  fun foobar(): String {
 *      return stateController.getStateInstance().foobar()
 *  }
 *
 *  inner class Start : SomeClassWithState {
 *      override val state: SomeClassWithState.State =
 *          SomeClassWithState.State.START
 *
 *      override fun foobar(): String {
 *          return "Start!"
 *      }
 *
 *      override suspend fun endState() {
 *          count.getAndIncrement()
 *          println("Start!")
 *      }
 *  }
 *
 *  inner class Doing : SomeClassWithState {
 *
 *      override val state: SomeClassWithState.State =
 *          SomeClassWithState.State.DOING
 *
 *      override fun foobar(): String {
 *          return "Doing!"
 *      }
 *
 *      override suspend fun startState() {
 *          count.getAndIncrement()
 *          println("Doing!")
 *      }
 *
 *      override suspend fun endState() {
 *          count.getAndIncrement()
 *          println("Done!")
 *      }
 *  }
 *
 *  inner class End : SomeClassWithState {
 *
 *      override val state: SomeClassWithState.State =
 *          SomeClassWithState.State.END
 *
 *      override fun foobar(): String {
 *          return "End!"
 *      }
 *
 *      override suspend fun startState() {
 *          count.getAndIncrement()
 *          println("End!")
 *      }
 *  }
 * }
 * ```
 *
 *
 * @param scope [ModuleScope] will provide a coroutine scope during the state transfering
 * @param parentStateClass is your parent state that would
 * be a receiver in state observer, and interceptor lambda
 * @see WithState
 */
open class StateController<S : Enum<*>, I : WithState<S>, C>(
    protected var scope: ModuleScope,
    protected var parentStateClass: C,
    firstState: I,
) {

    protected val currentState = atomic(firstState)

    private var observers = ConcurrentHashMap<suspend C.(S, S) -> Unit, ListenerState>()
    private var interceptors = ConcurrentHashMap<suspend C.(S, S) -> Boolean, ListenerState>()

    /**
     * Init state controller, to call the first state [WithState.startState]
     */
    suspend fun init() {
        currentState.value.startState()
    }

    /**
     * Get current enum state
     */
    fun getCurrentState(): S =
        currentState.value.state

    /**
     * Get current instance state
     */
    fun getStateInstance(): I {
        return currentState.value
    }

    /**
     * Transfer state from current state to specfied state
     *
     * It will call all observers in parallel, all interceptors in serial during transfering,
     * if there is an intercetpor with [ListenerState.BEFORE_UPDATE] priority,
     * it could intercept and cancel this transfering.
     *
     * @see ListenerState
     * @see observeStateChange
     * @see interceptStateChange
     * @param after transfer to this state, instance type [I]
     */
    suspend fun setState(after: I): I {
        val before = currentState.value
        val beforeState = before.state
        val afterState = after.state
        if (invokeChange(beforeState, afterState, ListenerState.BEFORE_UPDATE, parentStateClass)) return before
        before.endState()
        currentState.update { after }
        after.startState()
        invokeChange(beforeState, afterState, ListenerState.AFTER_UPDATE, parentStateClass)
        return before
    }

    private suspend fun invokeChange(
        beforeState: S,
        afterState: S,
        listenerState: ListenerState,
        listenerCaller: C,
    ): Boolean {
        var isIntercepted by atomic(false)
        observers
            .asSequence()
            .filter { it.value == listenerState }
            .forEach { (observer, _) ->
                scope.launch {
                    listenerCaller.observer(beforeState, afterState)
                }
            }
        interceptors
            .asSequence()
            .asFlow()
            .filter { it.value == listenerState }
            .map { (interceptor, _) ->
                scope.launch {
                    isIntercepted = listenerCaller.interceptor(beforeState, afterState) || isIntercepted
                }
            }
            .collect { it.join() }
        return isIntercepted
    }

    /**
     * Observe a state change
     *
     * [observer] will be called in parallel
     *
     * @param listenerState a [ListenerState] to indicate invocation priority
     * @param observer observation lambda block with [C] context
     * @see observe
     * @see setState
     */
    fun observeStateChange(
        listenerState: ListenerState = ListenerState.BEFORE_UPDATE,
        observer: suspend C.(S, S) -> Unit,
    ) {
        observers[observer] = listenerState
    }

    /**
     * Intercept a state change
     *
     * [interceptor] will be called in serial
     *
     * @param listenerState a [ListenerState] to indicate invocation priority
     * @param interceptor interception lambda block with [C] context
     * @see block
     * @see intercept
     * @see setState
     */
    fun interceptStateChange(
        listenerState: ListenerState = ListenerState.BEFORE_UPDATE,
        interceptor: suspend C.(S, S) -> Boolean,
    ) {
        interceptors[interceptor] = listenerState
    }

    @SorapointaInternal
    fun cleanAllObserver() {
        observers.clear()
    }

    @SorapointaInternal
    fun cleanAllInterceptor() {
        interceptors.clear()
    }

    /**
     * Observer or interceptor's priority
     *
     * [setState] will call all observers in parallel, all interceptors in serial during transfering,
     * if there is an intercetpor with [ListenerState.BEFORE_UPDATE] priority,
     * it could intercept and cancel the transfering.
     *
     * @see setState
     */
    enum class ListenerState {
        BEFORE_UPDATE,
        AFTER_UPDATE,
    }
}

/**
 * [S] is state enum, [I] is state interface, [C] is the class with state
 * @param stateInstances all instances of your different state classes
 */
class InitStateController<S : Enum<*>, I : WithState<S>, C>(
    scope: ModuleScope,
    parentStateClass: C,
    vararg stateInstances: I,
) : StateController<S, I, C>(scope, parentStateClass, stateInstances.first()) {

    private val states = listOf(*stateInstances)

    /**
     * Transfer state from current state to specfied state
     *
     * It will call all observers in parallel, all interceptors in serial during transfering,
     * if there is an intercetpor with [ListenerState.BEFORE_UPDATE] priority,
     * it could intercept and cancel this transfering.
     *
     * @see ListenerState
     * @see observeStateChange
     * @see interceptStateChange
     * @param afterState transfer to this state, enum type [S]
     */
    suspend fun setState(afterState: S): I =
        setState(states.first { it.state == afterState })
}

/**
 * Quick way of [StateController.observeStateChange]
 * [S] is state enum, [I] is state interface, [C] is the class with state
 *
 * @param listenerState a [ListenerState] to indicate invocation priority
 * @param observer observation lambda block with [C] context,
 * and without any input parameter
 * @see StateController.observeStateChange
 * @see StateController.setState
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <S : Enum<*>, I : WithState<S>, C>
    StateController<S, I, C>.observe(
        listenerState: ListenerState = ListenerState.BEFORE_UPDATE,
        noinline observer: suspend C.() -> Unit,
    ) = observeStateChange(listenerState) { _, _ -> this.observer() }

/**
 * Quick way of [StateController.interceptStateChange]
 * [S] is state enum, [I] is state interface, [C] is the class with state
 *
 * @param listenerState a [ListenerState] to indicate invocation priority
 * @param interceptor interception lambda block with [C] context
 * and without any input parameter
 * @see StateController.interceptStateChange
 * @see StateController.setState
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <S : Enum<*>, I : WithState<S>, C>
    StateController<S, I, C>.intercept(
        listenerState: ListenerState = ListenerState.BEFORE_UPDATE,
        noinline interceptor: suspend C.() -> Boolean,
    ) = interceptStateChange(listenerState) { _, _ -> this.interceptor() }

/**
 * Quick way of [StateController.interceptStateChange]
 * [S] is state enum, [I] is state interface, [C] is the class with state
 *
 * Observe changes and call [block] in serial
 *
 * @param listenerState a [ListenerState] to indicate invocation priority
 * @param block lambda block with [C] context
 * and without any input parameter, and final return
 * @see StateController.interceptStateChange
 * @see StateController.setState
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <S : Enum<*>, I : WithState<S>, C>
    StateController<S, I, C>.block(
        listenerState: ListenerState = ListenerState.BEFORE_UPDATE,
        noinline block: suspend C.() -> Unit,
    ) = interceptStateChange(listenerState) { _, _ -> this.block(); false }
