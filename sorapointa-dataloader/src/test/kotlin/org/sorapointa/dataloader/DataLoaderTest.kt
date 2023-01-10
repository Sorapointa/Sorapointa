package org.sorapointa.dataloader

import org.junit.jupiter.api.Test
import org.sorapointa.dataloader.def.avatarDataList
import org.sorapointa.utils.TestOption
import org.sorapointa.utils.runTest
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class DataLoaderTest {
    @OptIn(ExperimentalTime::class)
    @Test
    fun test() = runTest(TestOption.SKIP_CI) {
        ResourceHolder.apply {
            measureTime {
                findAndRegister()
            }.also { println("Costed: $it") }
            measureTime {
                loadAll()
            }.also { println("Costed: $it") }
        }
        println(avatarDataList)
    }
}
