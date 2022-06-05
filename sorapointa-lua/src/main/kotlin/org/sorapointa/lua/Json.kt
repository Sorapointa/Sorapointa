package org.sorapointa.lua

import kotlinx.serialization.json.*
import org.sorapointa.utils.uncheckedCast

/**
 * For lua interoperability
 */
internal val luaJson = Json {
    encodeDefaults = true
}

internal fun JsonElement.toLuaTable(): Any? {
    return when (this) {
        is JsonObject -> {
            toMap().mapValues { (_, v) -> v.toLuaTable() }
        }
        is JsonArray -> toList().map { it.toLuaTable() }
        is JsonPrimitive -> when {
            isString -> contentOrNull
            else -> longOrNull ?: doubleOrNull ?: booleanOrNull
        }
        is JsonNull -> null
    }
}

internal fun Any?.toJsonElement(): JsonElement {
    return when (this) {
        null -> JsonNull
        is String -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is List<*> -> JsonArray(map { it.toJsonElement() })
        is Map<*, *> -> {
            JsonObject(mapValues { it.value.toJsonElement() }.uncheckedCast())
        }
        else -> error("Illegal input")
    }
}
