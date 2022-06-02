package org.sorapointa.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * For the config purpose, as possible as lenient
 */
val prettyJson = Json {
    isLenient = true
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

/**
 * For the network or storage purpose,
 * strict but ignore the unknown key,
 * and without null fields
 */
@OptIn(ExperimentalSerializationApi::class)
val networkJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
    explicitNulls = false
}
