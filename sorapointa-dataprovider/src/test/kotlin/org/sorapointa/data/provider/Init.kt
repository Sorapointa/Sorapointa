package org.sorapointa.data.provider

import kotlinx.coroutines.runBlocking

fun initTestDataProvider(): Unit = runBlocking {
    DatabaseConfig.init()
    DatabaseManager.loadDatabase()
}
