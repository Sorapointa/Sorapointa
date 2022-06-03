package org.sorapointa.utils

import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import org.junit.jupiter.api.Test

class YamlCompatible {
    @Test
    fun `addtional field must be okay with filled default`() {
        @kotlinx.serialization.Serializable
        data class Field1(
            val xxxxx: String = "123123",
        )

        @kotlinx.serialization.Serializable
        data class Field2(
            val xxxxx: String = "123123",
            val yyyyy: String = "123123",
        )

        val before = Yaml.encodeToString(Field1())

        println(before)

        val after = Yaml.decodeFromString<Field2>(before)

        println(after)

        val reEncode = Yaml.encodeToString(after)

        println(reEncode)
    }

    @Test
    fun `ignore unknown keys`() {
        @kotlinx.serialization.Serializable
        data class Field1(
            val xxxxx: String = "123123",
        )

        val input = """
            xxxxx: 123123
            yyyyy: 123123
        """.trimIndent()

        Yaml.decodeFromString<Field1>(input).also(::println)
    }
}
