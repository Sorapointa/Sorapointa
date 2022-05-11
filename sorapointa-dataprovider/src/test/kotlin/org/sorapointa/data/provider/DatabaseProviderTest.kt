package org.sorapointa.data.provider

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import org.litote.kmongo.eq
import org.sorapointa.utils.TestOption
import org.sorapointa.utils.runTest
import kotlin.test.assertEquals

class DatabaseProviderTest {
    init {
        initTestDataProvider()
    }

    @Serializable
    data class TestData(
        @SerialName("_id") val id: Long,
        val level: Int
    )

    @Test
    fun databaseConnectTest() = runTest(TestOption.SKIP_CI) {
        val persist = DatabasePersist<TestData>("test")
        persist.data.insertOne(TestData(114514, 1010))
        assertEquals(
            1010,
            persist.data.findOne(TestData::id eq 114514L)?.level
        )
    }
}
