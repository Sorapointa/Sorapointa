package org.sorapointa.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * For config purpose, as possible as lenient
 */
val prettyJson = Json {
    isLenient = true
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

/**
 * For network or storage purpose, strict but ignore unknown key
 */
@OptIn(ExperimentalSerializationApi::class)
val networkJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
    explicitNulls = false
}
