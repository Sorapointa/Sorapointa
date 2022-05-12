package org.sorapointa.task

import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.sorapointa.utils.now

// @Serializable
// data class CronTask(
//    @SerialName("_id")
//    val id: String,
//    val cron: CronWrapper,
//    val lastExecution: Instant? = null,
// ) {

// }

object CronTasks : IdTable<String>("cron_tasks") {
    override val id = varchar("id", 128).entityId()

    //    override val id = varchar("id", 80)
    val cron: Column<String> = varchar("cron", 80)
    val lastExecution = this.timestamp("last_execution").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

class CronTask(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, CronTask>(CronTasks)

    var cron by CronTasks.cron.transform(
        toColumn = {
            it.asString()
        },
        toReal = {
            parseCron(it)
        }
    )

    var lastExecution by CronTasks.lastExecution

    fun nextExecution(time: Instant = now()): Instant =
        cron.nextExecutionTime(lastExecution ?: time) ?: time
}
