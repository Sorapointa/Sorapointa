package org.sorapointa.utils

import java.util.concurrent.ConcurrentHashMap

inline fun <reified K, reified V> buildConcurrencyMap(
    initCapacity: Int = 16,
    builder: ConcurrentHashMap<K, V>.() -> Unit,
): ConcurrentHashMap<K, V> {
    val map = ConcurrentHashMap<K, V>(initCapacity)
    map.builder()
    return map
}
