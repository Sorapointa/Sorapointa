package org.sorapointa.utils

import kotlinx.coroutines.runBlocking

/**
 * JVM shutdown hook
 * @param block lambda will be executed after program exited
 */
fun addShutdownHook(block: suspend () -> Unit) {
    Runtime.getRuntime().addShutdownHook(
        Thread {
            runBlocking { block() }
        }
    )
}
