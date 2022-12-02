package org.sorapointa.utils

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.junit.jupiter.api.Test

class YamlCompatible {
    @Test
    fun `addtional field must be okay with filled default`() {
        @Serializable
        data class Field1(
            val xxxxx: String = "123123",
        )

        @Serializable
        data class Field2(
            val xxxxx: String = "123123",
            val yyyyy: String = "123123",
        )

        Yaml(configuration = YamlConfiguration())

        val before = lenientYaml.encodeToString(Field1.serializer(), Field1())

        println(before)

        val after = lenientYaml.decodeFromString<Field2>(before)

        println(after)

        val reEncode = lenientYaml.encodeToString(Field2.serializer(), after)

        println(reEncode)
    }

    @Test
    fun `ignore unknown keys`() {
        @Serializable
        data class Field1(
            val xxxxx: String = "123123",
        )

        val input = """
            xxxxx: 123123
            yyyyy: 123123
        """.trimIndent()

        lenientYaml.decodeFromString(Field1.serializer(), input).also(::println)
    }
}
