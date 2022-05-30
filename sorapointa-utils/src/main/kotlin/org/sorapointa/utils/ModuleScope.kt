package org.sorapointa.utils

import kotlinx.coroutines.*
import mu.KLogger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

open class ModuleScope(
    private val logger: KLogger,
    private val moduleName: String,
    parentContext: CoroutineContext = EmptyCoroutineContext,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, e -> logger.error(e) { "Caught Exception on $moduleName" } },
) : CoroutineScope {

    val parentJob = SupervisorJob(parentContext[Job])

    override val coroutineContext: CoroutineContext =
        parentContext + parentJob + CoroutineName(moduleName) + exceptionHandler + dispatcher

    fun dispose() {
        parentJob.cancel()
        onClosed()
    }

    open fun onClosed() {
    }
}
