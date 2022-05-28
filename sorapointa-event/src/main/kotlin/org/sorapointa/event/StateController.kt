@file:Suppress( "unused")

package org.sorapointa.event

import org.sorapointa.utils.ModuleScope

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap


interface WithState<out T: Enum<*>> {

    val state: T

    suspend fun startState() {

    }

    suspend fun endState() {

    }

}

class StateController<TState : Enum<*>, TInterfaceWithState: WithState<TState>, TClassWithState> (
    private var scope: ModuleScope,
    private var parentStateClass: TClassWithState,
    vararg stateInstances: TInterfaceWithState
) {

    private val states = listOf(*stateInstances)

    private var currentState = atomic(states.first())

    private var observers = ConcurrentHashMap<suspend TClassWithState.(TState, TState) -> Unit, ListenerState>()
    private var interceptors = ConcurrentHashMap<suspend TClassWithState.(TState, TState) -> Boolean, ListenerState>()


    init {
        // TODO: Check it whether needs to be extracted as a single function
        runBlocking {
            currentState.value.startState()
        }
    }

    suspend fun setState(afterState: TState): TInterfaceWithState {
        val before = currentState.value
        val beforeState = before.state
        val after = states.first { it.state == afterState }
        if (invokeChange(beforeState, afterState, ListenerState.BEFORE_UPDATE, parentStateClass)) return before
        before.endState()
        currentState.update { after }
        after.startState()
        invokeChange(beforeState, afterState, ListenerState.AFTER_UPDATE, parentStateClass)
        return before
    }

    private suspend fun invokeChange(
        beforeState: TState,
        afterState: TState,
        listenerState: ListenerState,
        listenerCaller: TClassWithState
    ): Boolean {
        var isIntercepted by atomic(false)
        observers
            .asSequence()
            .filter { it.value == listenerState }
            .forEach {(observer, _) ->
                scope.launch {
                    listenerCaller.observer(beforeState, afterState)
                }
            }
        interceptors
            .asSequence()
            .asFlow()
            .filter { it.value == listenerState }
            .map {(interceptor, _) ->
                scope.launch {
                    isIntercepted = listenerCaller.interceptor(beforeState, afterState) || isIntercepted
                }
            }
            .collect { it.join() }
        return isIntercepted
    }

    fun getState(): TInterfaceWithState {
        return currentState.value
    }

    fun observeStateChange(
        listenerState: ListenerState = ListenerState.BEFORE_UPDATE,
        observer: suspend TClassWithState.(TState, TState) -> Unit
    ) {
        observers[observer] = listenerState
    }

    fun interceptStateChange(
        listenerState: ListenerState = ListenerState.BEFORE_UPDATE,
        interceptor: suspend TClassWithState.(TState, TState) -> Boolean
    ) {
        interceptors[interceptor] = listenerState
    }

    fun cleanAllObserver() {
        observers.clear()
    }

    fun cleanAllInterceptor() {
        interceptors.clear()
    }

    enum class ListenerState {
        BEFORE_UPDATE,
        AFTER_UPDATE
    }


}


@Suppress("NOTHING_TO_INLINE")
inline fun <TState : Enum<*>, TInterfaceWithState: WithState<TState>, TClassWithState>
    StateController<TState, TInterfaceWithState, TClassWithState>.observe(
    listenerState: StateController.ListenerState = StateController.ListenerState.BEFORE_UPDATE,
    noinline observer: suspend TClassWithState.() -> Unit
) = observeStateChange(listenerState) { _, _ -> this.observer() }

@Suppress("NOTHING_TO_INLINE")
inline fun <TState : Enum<*>, TInterfaceWithState: WithState<TState>, TClassWithState>
    StateController<TState, TInterfaceWithState, TClassWithState>.intercept(
    listenerState: StateController.ListenerState = StateController.ListenerState.BEFORE_UPDATE,
    noinline interceptor: suspend TClassWithState.() -> Boolean
) = interceptStateChange(listenerState) { _, _ -> this.interceptor() }

@Suppress("NOTHING_TO_INLINE")
inline fun <TState : Enum<*>, TInterfaceWithState: WithState<TState>, TClassWithState>
    StateController<TState, TInterfaceWithState, TClassWithState>.block(
    listenerState: StateController.ListenerState = StateController.ListenerState.BEFORE_UPDATE,
    noinline interceptor: suspend TClassWithState.() -> Unit
) = interceptStateChange(listenerState) { _, _ -> this.interceptor(); false }

