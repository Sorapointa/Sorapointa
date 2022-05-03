package org.sorapointa.utils

import kotlin.contracts.contract

public inline fun <reified T> Any?.cast(): T {
    contract { returns() implies (this@cast is T) }
    return this as T
}

public inline fun <reified U : T, T> T.castUp(): U {
    contract { returns() implies (this@castUp is U) }
    return this as U
}

public inline fun <reified T> Any?.safeCast(): T? {
    contract { returnsNotNull() implies (this@safeCast is T) }
    return this as? T
}

public inline fun <reified T> Any?.castOrNull(): T? {
    contract { returnsNotNull() implies (this@castOrNull is T) }
    return this as? T
}

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
public inline fun <T> Any?.uncheckedCast(): T = this as T
