package org.sorapointa.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.TestOnly

private val logger = mu.KotlinLogging.logger { }

internal val isCI by lazy {
    val ci = System.getenv("CI") != null
    ci.also {
        logger.info { "Sorapointa run in CI" }
    }
}

enum class TestOption {
    SKIP_CI,
}

@TestOnly
fun <T> runTest(vararg option: TestOption = emptyArray(), block: suspend CoroutineScope.() -> T) {
    when {
        option.contains(TestOption.SKIP_CI) && isCI -> return
    }

    runBlocking(block = block)
}

fun isJUnitTest(): Boolean {
    for (element in Thread.currentThread().stackTrace) {
        if (element.className.startsWith("org.junit.")) {
            return true
        }
    }
    return false
}
