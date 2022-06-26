@file:Suppress("NOTHING_TO_INLINE")

package org.sorapointa.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.sorapointa.SorapointaConfig
import org.sorapointa.dataloader.common.FightProp
import org.sorapointa.dataloader.common.PlayerProp
import org.sorapointa.proto.fightPropPair
import org.sorapointa.proto.propPair
import org.sorapointa.proto.propValue
import kotlin.time.DurationUnit
import kotlin.time.toDuration

inline fun calculateAvatarStats(base: Float, extra: Float, extraPercent: Float) =
    base + extra + (base * extraPercent)

inline infix fun PlayerProp.map(value: Int) =
    this.value to value(value)

inline infix fun PlayerProp.map(value: Long) =
    this.value to value(value)

inline infix fun PlayerProp.value(value: Int) =
    this@value.value(value.toLong())

inline infix fun PlayerProp.value(value: Long) =
    propValue {
        type = this@value.value
        ival = value
        val_ = value
    }

inline infix fun PlayerProp.pair(value: Int) =
    pair(value.toLong())

inline infix fun PlayerProp.pair(value: Long) =
    propPair {
        type = this@pair.value
        propValue = this@pair.value(value)
    }

inline infix fun FightProp.map(value: Int) =
    map(value.toFloat())

inline infix fun FightProp.map(value: Float) =
    this.value to value

inline infix fun FightProp.pair(value: Int) =
    pair(value.toFloat())

inline infix fun FightProp.pair(value: Float) =
    fightPropPair {
        propType = this@pair.value
        propValue = value
    }

inline infix fun Int.fightProp(value: Int) =
    fightProp(value.toFloat())

inline infix fun Int.fightProp(value: Float) =
    fightPropPair {
        propType = this@fightProp
        propValue = value
    }


val todayStartTime: Instant
    get() = run {
        val offsetHour = SorapointaConfig.data.offsetHours
        val timeZone = SorapointaConfig.data.timeZone
        val nowDate = now().toLocalDateTime(timeZone)
        LocalDateTime(nowDate.year, nowDate.month, nowDate.dayOfMonth, offsetHour, 0)
            .toInstant(timeZone)
            .let {
                if (nowDate.hour < offsetHour) {
                    it - 1.toDuration(DurationUnit.DAYS)
                } else {
                    it
                }
            }
    }
