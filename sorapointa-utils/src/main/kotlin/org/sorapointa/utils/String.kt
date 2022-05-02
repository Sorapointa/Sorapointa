package org.sorapointa.utils

import java.io.Serializable

fun String.replaceWithOrder(vararg args: Serializable?): String {
    if (args.isEmpty() || isEmpty()) {
        return this
    }
    val chars = toCharArray()
    val builder = StringBuilder(length)
    var i = 0
    while (i < chars.size) {
        val mark = i
        if (chars[i] == '{') {
            var num = 0
            while (i + 1 < chars.size && Character.isDigit(chars[i + 1])) {
                i++
                num *= 10
                num += chars[i] - '0'
            }
            if (i != mark && i + 1 < chars.size && chars[i + 1] == '}') {
                i++
                builder.append(args.getOrNull(num)?.toString() ?: "{$num}")
            } else {
                i = mark
            }
        }
        if (mark == i) {
            builder.append(chars[i])
        }
        i++
    }
    return builder.toString()
}
