package org.sorapointa.task

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.sorapointa.data.provider.DatabaseManager
import org.sorapointa.data.provider.initTestDataProvider
import org.sorapointa.task.TaskManager.tasks
import org.sorapointa.utils.TestOption
import org.sorapointa.utils.runTest
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskManagerTest {
    @BeforeAll
    fun init() = runTest(TestOption.SKIP_CI) {
        initTestDataProvider()
        println(DatabaseManager.defaultDatabaseName)
        TaskManager.init()
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
    fun `register same key, the cron must be the later one's`() = runTest(TestOption.SKIP_CI) {
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

        assertEquals("* * * * *", tasks.findOneById("test1")?.cron?.cron?.asString())
    }


    @Test
    fun `high volume test`() = runTest(TestOption.SKIP_CI) {
        val acc = atomic(0)

        val intRange = (1..10000).toList()
        launch {
            intRange.mapNotNull {
                TaskManager.registerTask("high volume test $it", "* * * * *") {
                    acc.getAndIncrement()
                    delay(1000)
                    cancel()
                }
            }.joinAll()
        }.join()

        assertEquals(intRange.size, acc.value)
    }
}
