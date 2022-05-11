package org.sorapointa.data.provider

import org.sorapointa.utils.TestOption
import org.sorapointa.utils.runTest
import kotlin.reflect.jvm.javaField

fun initTestDataProvider(): Unit = runTest(TestOption.SKIP_CI) {
    val field = DatabaseConfig.data::defaultDatabaseName.javaField!!
    field.trySetAccessible()
    field.set(DatabaseConfig.data, "test")
    DatabaseManager.getDatabase("test").drop()
}
