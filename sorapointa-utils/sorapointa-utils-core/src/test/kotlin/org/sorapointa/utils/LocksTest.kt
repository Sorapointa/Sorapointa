package org.sorapointa.utils

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test

class LocksTest {

    private val mutex = Mutex()

    @Test
    fun `test mutex lock with coroutine`() = runBlocking {
        withTimeout(1000) {
            mutex.withReentrantLock {
                doSomeWork()
            }
        }
    }

    suspend fun doSomeWork() {
        mutex.withReentrantLock {
            println("Working!")
        }
    }
}
