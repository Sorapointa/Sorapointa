package org.sorapointa.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private val logger = KotlinLogging.logger {}

class ScopeTest {

    @Test
    fun `scope test`(): Unit = runBlocking {

        val parentScope = ModuleScope("TestParent")

        val moduleScope = ModuleScope("TestModule", parentScope.coroutineContext)
        val moduleScope2 = ModuleScope("TestModule2", parentScope.coroutineContext)

        parentScope.launch {
            logger.info { "Running... Parent Scope ${this.coroutineContext}" }
            logger.info { "Parent Children: ${parentScope.parentJob.children.joinToString(" | ")}" }
            delay(500)
        }

        moduleScope.launch {
            delay(200)
        }

        moduleScope2.launch {
            delay(200)
        }

        assertEquals(3, parentScope.parentJob.children.toList().size)
    }
}
