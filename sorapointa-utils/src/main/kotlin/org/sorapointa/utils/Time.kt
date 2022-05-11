package org.sorapointa.utils

import kotlinx.datetime.*
import java.time.ZonedDateTime

/**
 * Just a fast path for [Clock.System.now]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun now() = Clock.System.now()

/**
 * Convert KotlinX [Instant] to Java [ZonedDateTime]
 */
fun Instant.toZonedUtc(): ZonedDateTime = toJavaInstant().atZone(TimeZone.UTC.toJavaZoneId())
