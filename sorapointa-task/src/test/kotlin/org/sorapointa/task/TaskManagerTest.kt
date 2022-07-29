package org.sorapointa.task

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import org.sorapointa.data.provider.DatabaseManager
import org.sorapointa.data.provider.initTestDataProvider
import org.sorapointa.utils.TestOption
import org.sorapointa.utils.runTest
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskManagerTest {

    @BeforeAll
    fun init() = runTest(TestOption.SKIP_CI) {
        initTestDataProvider()
        TaskManager.init()
        DatabaseManager.loadTables(CronTasks)
    }

    @Test
    fun `register same key, only first work`() = runTest(TestOption.SKIP_CI) {
        val job1 = TaskManager.registerTask("test2", "* * * * *") {}
        delay(200)
        val job2 = TaskManager.registerTask("test2", "* * * * *") {}
        delay(200)
        assert(job1?.isActive == true)
        assert(job2 == null)
        delay(2000)
        job1?.cancel()
        job2?.cancel()
    }

    @Test
    fun `register same key, the cron must be the first one's`() = runTest(TestOption.SKIP_CI) {
        val job1 = TaskManager.registerTask("test1", "* * * * *") {}
        delay(200)
        val job2 = TaskManager.registerTask("test1", "30 * * * *") {}
        delay(200)

        launch {
            delay(1000)
            job1?.cancel()
            job2?.cancel()
        }

        listOfNotNull(job1, job2).joinAll()

        transaction {
            assertEquals("* * * * *", CronTask.findById("test1")?.cron?.asString())
        }
    }

    @Test
    @Timeout(3, unit = TimeUnit.MINUTES)
    fun `high volume test`() = runTest(TestOption.SKIP_CI) {
        val acc = atomic(0)

        val testCount = 10000
        val intRange = (1..testCount).toList()
        launch {
            intRange.mapNotNull {
                TaskManager.registerTask("high volume test $it", "* * * * *") {
                    acc.getAndIncrement()
                }
            }.joinAll()
        }

        launch {
            while (isActive) {
                println("Current value: ${acc.value}")
                delay(500)
            }
        }.join()

        assertEquals(intRange.size, acc.value)
    }
}
