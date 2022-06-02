package org.sorapointa.utils

import java.util.*

/**
 * Unwrap Java [Optional] to kotlin nullable type
 */
fun <T> Optional<T>.unwrap(): T? = orElse(null)
