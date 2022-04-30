package org.sorapointa.utils

import kotlinx.coroutines.runBlocking

fun addShutdownHook(block: suspend () -> Unit) {
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking { block() }
    })
}
