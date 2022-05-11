package org.sorapointa.task

import com.cronutils.model.Cron
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import org.sorapointa.data.provider.DatabasePersist
import org.sorapointa.data.provider.findOneOrInsertDefault
import org.sorapointa.utils.ModuleScope
import org.sorapointa.utils.now
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

object TaskManager {
    private var scope = ModuleScope(logger, "TaskManager")

    private val cronJobMap: MutableMap<String, Job> = ConcurrentHashMap()

    fun init(parentContext: CoroutineContext = EmptyCoroutineContext) {
        scope = ModuleScope(logger, "TaskManager", parentContext)
    }

    fun close() {
        scope.cancel("Closing")
    }

    internal val tasks = DatabasePersist<CronTask>("tasks").data

    fun registerTask(
        delay: Duration,
        task: suspend () -> Unit,
    ): Job = scope.launch {
        while (isActive) {
            task()
            delay(delay)
        }
    }

    fun registerTask(
        delayMillis: Long,
        task: suspend () -> Unit,
    ): Job = scope.launch {
        while (isActive) {
            task()
            delay(delayMillis)
        }
    }

    fun registerTask(
        id: String,
        cron: String,
        task: suspend () -> Unit,
    ): Job? = registerTask(id, parseCron(cron), task)

    fun registerTask(
        id: String,
        cron: Cron,
        task: suspend () -> Unit,
    ): Job? {
        // if not null, cancel job store in map
        if (cronJobMap[id] != null) {
            logger.warn { "Conflicted cron task id '$id', return null..." }
            return null
        }

        return scope.launch {
            val default by lazy { CronTask(id, cron.wrap()) }

            val found = tasks.findOneOrInsertDefault(id, default)

            if (found.cron.cron != cron) {
                tasks.findOneAndReplace(CronTask::id eq id, default)
            }

            while (isActive) {
                val beforeTime = now()
                val taskData = tasks.findOneById(id) ?: error("Failed to get task data for id $id")
                delay(taskData.nextExecution(beforeTime) - beforeTime)

                listOf(
                    launch {
                        task()
                    },
                    launch {
                        tasks.findOneAndUpdate(
                            CronTask::id eq id, setValue(CronTask::lastExecution, now())
                        )
                    }
                ).joinAll()
            }
        }.also {
            cronJobMap[id] = it
        }
    }
}
