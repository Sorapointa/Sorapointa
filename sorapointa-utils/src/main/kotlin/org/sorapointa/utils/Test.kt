package org.sorapointa.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.TestOnly
import org.sorapointa.config.IS_CI
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

const val isCI = IS_CI

enum class TestOption {
    SKIP_CI,
}

@TestOnly
fun <T> runTest(vararg option: TestOption = emptyArray(), block: suspend CoroutineScope.() -> T) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    when {
        option.contains(TestOption.SKIP_CI) && isCI -> return
    }

    runBlocking(block = block)
}

/**
 *
 */
fun isJUnitTest(): Boolean {
    for (element in Thread.currentThread().stackTrace) {
        if (element.className.startsWith("org.junit.")) {
            return true
        }
    }
    return false
}
