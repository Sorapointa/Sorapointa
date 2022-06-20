@file:Suppress("NOTHING_TO_INLINE")

package org.sorapointa.utils.encoding

inline fun bkdrHash(str: String): Int =
    str.fold(0) { acc, char -> char.code + 131 * acc }

inline fun getHashByPrefixSuffix(prefix: Int, suffix: Long): Long =
    ((prefix.toLong()) shl 32) or suffix
