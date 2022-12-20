package org.sorapointa.task

import com.cronutils.model.Cron
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.sorapointa.utils.ModuleScope
import org.sorapointa.utils.now
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

interface ITaskManager {

    /**
     * Init task manager scope for structured concurrency
     *
     * This method **IS NOT** thread-safe
     */
    fun init(parentContext: CoroutineContext = EmptyCoroutineContext)

    fun close()

    /**
     * Register a simple delayed task
     *
     * @return task job
     */
    fun registerTask(
        delay: Duration,
        task: suspend () -> Unit,
    ): Job

    /**
     * Register a simple delayed task
     *
     * @return task job
     */
    fun registerTask(
        delayMillis: Long,
        task: suspend () -> Unit,
    ): Job

    /**
     * Register a task
     *
     * @return If this job has been registered already, it will return null
     */
    fun registerTask(
        id: String,
        cron: String,
        task: suspend () -> Unit,
    ): Job?

    /**
     * Register a task
     *
     * @return If this job has been registered already, it will return null
     */
    fun registerTask(
        id: String,
        cron: Cron,
        task: suspend () -> Unit,
    ): Job?
}

object TaskManager : ITaskManager {
    private var scope = ModuleScope("TaskManager")

    private val cronJobMap: MutableMap<String, Job> = ConcurrentHashMap()

    /**
     * Init task manager scope for structured concurrency
     *
     * This method **IS NOT** thread-safe
     */
    override fun init(parentContext: CoroutineContext) {
        scope = ModuleScope("TaskManager", parentContext)
    }

    override fun close() {
        scope.cancel("Closing")
    }

    /**
     * Register a simple delayed task
     *
     * @return task job
     */
    override fun registerTask(
        delay: Duration,
        task: suspend () -> Unit,
    ): Job = scope.launch {
        while (isActive) {
            task()
            delay(delay)
        }
    }

    /**
     * Register a simple delayed task
     *
     * @return task job
     */
    override fun registerTask(
        delayMillis: Long,
        task: suspend () -> Unit,
    ): Job = scope.launch {
        while (isActive) {
            task()
            delay(delayMillis)
        }
    }

    /**
     * Register a task
     *
     * @return If this job has been registered already, it will return null
     */
    override fun registerTask(
        id: String,
        cron: String,
        task: suspend () -> Unit,
    ): Job? = registerTask(id, parseCron(cron), task)

    /**
     * Register a task
     *
     * @return If this job has been registered already, it will return null
     */
    override fun registerTask(
        id: String,
        cron: Cron,
        task: suspend () -> Unit,
    ): Job? {
        if (cronJobMap[id] != null) {
            logger.warn { "Conflicted cron task id '$id', return null..." }
            return null
        }

        return scope.launch {
            transaction {
                val found = CronTask.findById(id) ?: CronTask.new(id) { this.cron = cron }
                found.cron = cron
            }

            while (isActive) {
                val beforeTime = now()
                val taskData = transaction {
                    CronTask.findById(id) ?: error("Failed to get task data for id $id")
                }

                delay(taskData.nextExecution(beforeTime) - beforeTime)

                listOf(
                    launch {
                        task()
                    },
                    launch {
                        transaction {
                            taskData.lastExecution = now()
                        }
                    }
                ).joinAll()
            }
        }.also {
            cronJobMap[id] = it
        }
    }
}
