package org.sorapointa.data.provider

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.sorapointa.utils.prettyJson
import org.sorapointa.utils.readTextBuffered
import java.io.File
import kotlin.test.assertEquals
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class FileProviderTest {
    @Test
    fun readOnlyTest() = runBlocking {
        @kotlinx.serialization.Serializable
        data class TestConfig(
            private val test: String = "114514"
        )

        val file = File("./tmp/read-only-provider-test.json")
        file.apply { if (exists()) delete() }
        val config = object : ReadOnlyFilePersist<TestConfig>(file, TestConfig()) {}
        config.reload()
        println(config.data)
    }

    @Test
    fun autoSaveTest() = runBlocking {
        @kotlinx.serialization.Serializable
        data class TestConfig(
            var test: String = "114514",
            var foo: Int = 123123,
        )

        val file = File("./tmp/auto-save-provider-test.json")
        file.apply { if (exists()) delete() }
        val config = object : AutoSaveFilePersist<TestConfig>(
            file,
            TestConfig(),
            5.toDuration(DurationUnit.SECONDS),
        ) {}
        config.reload()
        println(config.data)
        config.data = config.data.apply {
            test = "2222"
            foo = 1111
        }
        delay(10_000)
        assertEquals(config.data, prettyJson.decodeFromString(TestConfig.serializer(), file.readTextBuffered()))
    }

    @Test
    fun autoScanTest() = runBlocking {
        @kotlinx.serialization.Serializable
        data class AutoScanData(
            var foo: Long = 1,
            var bar: Int = 1,
        )

        val file = File("./tmp/auto-save-provider-test.json")
        file.apply { if (exists()) delete() }
        val config = object : AutoScanFilePersist<AutoScanData>(
            file,
            AutoScanData(),
            5.toDuration(DurationUnit.SECONDS),
        ) {}
        config.reload()
        println(config.data)
        val writeJson = """
            {
                "foo": 2222,
                "bar": 1111
            }
        """
        file.writeText(writeJson.trimIndent())
        delay(10_000)
        assertEquals(config.data, prettyJson.decodeFromString(AutoScanData.serializer(), writeJson))
    }
}
