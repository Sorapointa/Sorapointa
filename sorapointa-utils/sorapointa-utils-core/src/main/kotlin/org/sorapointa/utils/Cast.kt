@file:Suppress("unused", "NOTHING_TO_INLINE")

package org.sorapointa.utils

import kotlin.contracts.contract

inline fun <reified T> Any?.safeCast(): T? {
    contract { returnsNotNull() implies (this@safeCast is T) }
    return this as? T
}

@Suppress("UNCHECKED_CAST")
inline fun <T> Any?.uncheckedCast(): T = this as T

inline fun Boolean.toInt(): Int =
    if (this) 1 else 0

inline fun Boolean.toUInt(): UInt =
    if (this) 1u else 0u

inline fun Boolean.toUShort(): UShort =
    if (this) 1u else 0u
