package org.sorapointa.utils.encoding

private val hexChars by lazy { "0123456789abcdef".toCharArray() }

/**
 * Encode byte array to hex
 */
@Suppress("MagicNumber")
val ByteArray.hex: String
    // Experimental Code, replace when stabled
    // get() = asUByteArray().joinToString("") { it.toString(radix = 16).padStart(2, '0') }
    get() {
        val hex = CharArray(2 * this.size)
        this.forEachIndexed { i, byte ->
            val unsigned = 0xff and byte.toInt()
            hex[2 * i] = hexChars[unsigned / 16]
            hex[2 * i + 1] = hexChars[unsigned % 16]
        }

        return hex.joinToString("")
    }
