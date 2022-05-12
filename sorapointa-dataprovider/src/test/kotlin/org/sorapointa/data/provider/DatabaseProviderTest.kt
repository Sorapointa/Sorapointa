package org.sorapointa.data.provider

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.sorapointa.utils.TestOption
import org.sorapointa.utils.runTest
import kotlin.test.assertEquals

object TestTable : Table("test_table") {
    val id: Column<Long> = long("id")
    val testString: Column<String> = varchar("test_string", 80)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseProviderTest {
    @Test
    fun databaseConnectTest() = runTest(TestOption.SKIP_CI) {
        initTestDataProvider()

        DatabaseManager.loadTables(TestTable)
        val s = "🤣"
        transaction(DatabaseManager.database) {
            TestTable.insert {
                it[id] = 1145141
                it[testString] = s
            }
            val selected = TestTable.select {
                TestTable.id eq 114514
            }.map {
                it[TestTable.testString]
            }.first()
            assertEquals(s, selected)
        }
    }
}
