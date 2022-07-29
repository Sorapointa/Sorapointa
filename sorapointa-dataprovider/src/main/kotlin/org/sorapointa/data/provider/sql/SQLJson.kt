package org.sorapointa.data.provider.sql

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject
import org.sorapointa.utils.networkJson

class JsonColumnType<out T : Any>(
    private val parser: JsonParser<T>,
) : ColumnType() {
    override fun sqlType() = "jsonb"

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        stmt[index] = PGobject().apply {
            this.type = sqlType()
            this.value = value as String?
        }
    }

    override fun valueFromDB(value: Any) = when (value) {
        is String -> parser.parse(value) // SQLite Support
        is PGobject -> parser.parse(value.value!!) // Postgres Support, nullable was processed by Exposed
        is Map<*, *> -> value
        else -> throw IllegalArgumentException("Unexpected value type ${value::class}")
    }

    @Suppress("UNCHECKED_CAST")
    override fun notNullValueToDB(value: Any) = parser.stringify(value as T)

    @Suppress("UNCHECKED_CAST")
    override fun nonNullValueToString(value: Any) = "'${parser.stringify(value as T)}'"
}

inline fun <reified T : Any> Table.jsonb(
    name: String,
    json: Json = networkJson
): Column<T> = registerColumn(name, JsonColumnType(JsonParser<T>(serializer(), json)))

class JsonParser<T : Any>(private val serializer: KSerializer<T>, private val json: Json = networkJson) {
    fun parse(string: String): T = json.decodeFromString(serializer, string)
    fun stringify(value: T) = json.encodeToString(serializer, value)
}
