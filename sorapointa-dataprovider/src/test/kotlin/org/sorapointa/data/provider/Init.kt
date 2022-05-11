package org.sorapointa.data.provider

import kotlinx.coroutines.runBlocking
import kotlin.reflect.jvm.javaField

fun initTestDataProvider(): Unit = runBlocking {
    val field = DatabaseConfig.data::defaultDatabaseName.javaField!!
    field.trySetAccessible()
    field.set(DatabaseConfig.data, "test")
    DatabaseManager.getDatabase("test").drop()
}
