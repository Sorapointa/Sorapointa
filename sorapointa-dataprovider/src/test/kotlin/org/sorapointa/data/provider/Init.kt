package org.sorapointa.data.provider

import kotlinx.coroutines.runBlocking
import org.sorapointa.config.TEST_CONNECTION
import org.sorapointa.config.TEST_DATABASE_PROVIDER
import kotlin.reflect.jvm.javaField

fun initTestDataProvider(): Unit = runBlocking {
    DatabaseConfig.init()
    val type = DatabaseConfig.data::type.javaField!!
    type.trySetAccessible()
    type.set(DatabaseConfig.data, TEST_DATABASE_PROVIDER)
    val connection = DatabaseConfig.data::connectionString.javaField!!
    connection.trySetAccessible()
    connection.set(DatabaseConfig.data, TEST_CONNECTION)
    DatabaseManager.loadDatabase()
}
