package org.sorapointa.task

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.utils.now

@Serializable
data class CronTask(
    @SerialName("_id")
    val id: String,
    val cron: CronWrapper,
    val lastExecution: Instant? = null,
) {
    fun nextExecution(time: Instant = now()): Instant =
        cron.cron.nextExecutionTime(lastExecution ?: time) ?: time
}
