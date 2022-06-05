package org.sorapinta.lua

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.Test
import org.sorapointa.lua.luaJson
import org.sorapointa.lua.toJsonElement
import org.sorapointa.lua.toLuaTable
import org.sorapointa.utils.uncheckedCast
import kotlin.test.assertEquals

class JsonTest {

    @Serializable
    data class ComplexData(
        val a: Short = 12123,
        val b: String = "xxx",
        val c: Data? = Data(),
    ) {
        @Serializable
        data class Data(
            val d: String = "xxxaadsf",
            val e: Boolean = true,
            val f: Boolean? = null,
            val h: Boolean = false,
            val i: Boolean = false,
            val j: String = "asdfadsf",
            val k: Boolean = true,
            val l: UShort = 12123u,
            val r: String = "adfadsfads",
            val s: String = "adsfafsd",
            val t: String = "asdfasdf",
            val u: String = "adfasdf",
            val v: Boolean = true,
            val w: List<String> = listOf("asdfadf", "asdfasdf"),
            val x: Map<String, String> = mapOf(
                "adsfadsf" to "asdfasdfas",
                "adsfasdfasdf" to "asdfasdf"
            ),
            val y: Map<String, Nested> = mapOf(
                "adsfasdf" to Nested("asdfadsf", 12312u)
            )
        )
        @Serializable
        data class Nested(
            val z: String,
            val a1: UInt
        )
    }

    @Test
    fun `to lua table and reverse`() {
        val data = ComplexData()
        val map = luaJson
            .encodeToJsonElement(data)
            .toLuaTable().also { println(it) }
            .uncheckedCast<Map<String, Any>>()
        val decoded = luaJson
            .decodeFromJsonElement<ComplexData>(map.toJsonElement())
            .also { println(it) }
        assertEquals(data, decoded)
    }

}
