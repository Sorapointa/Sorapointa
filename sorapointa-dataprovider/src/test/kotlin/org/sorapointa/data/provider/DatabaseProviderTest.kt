package org.sorapointa.data.provider

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import org.litote.kmongo.eq
import kotlin.test.assertEquals

class DatabaseProviderTest {
    @Serializable
    data class TestData(
        @SerialName("_id") val id: Long,
        val level: Int
    )

    @Test
    fun databaseConnectTest() = runBlocking {
        val persist = DatabasePersist<TestData>("test")
        persist.data.insertOne(TestData(114514, 1010))
        assertEquals(
            1010,
            persist.data.findOne(TestData::id eq 114514L)?.level
        )
    }
}
