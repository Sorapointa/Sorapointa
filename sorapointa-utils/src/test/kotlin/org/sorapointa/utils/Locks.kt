package org.sorapointa.utils

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Test

class Locks {

    private val mutex = Mutex()

    @Test
    fun `test mutex lock with coroutine`() = runBlocking {
        mutex.withReentrantLock {
            doSomeWork()
        }
    }

    suspend fun doSomeWork() {
        mutex.withReentrantLock {
            println("Working!")
        }
    }

}
