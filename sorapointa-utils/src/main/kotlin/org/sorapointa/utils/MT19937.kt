package org.sorapointa.utils

/**
 * MT19937 Mersenne Twister Random Generator
 * @property seed random seed
 * Author: HolographicHat
 */

class MT19937 private constructor(seed: ULong) {

    private var mti = 312
    private val mt = Array<ULong>(624) { 0U }

    init {
        mt[0] = seed
        mti = 1
        while (mti < 312) {
            mt[mti] = mti + 6364136223846793005UL * (mt[mti - 1] xor (mt[mti - 1] shr 62))
            mti += 1
        }
    }

    private fun mag(v: ULong) = if ((v and 1UL) == 0UL) 0UL else 13043109905998158313UL

    private fun g1() {
        for (i in 312 until 624) {
            val v9 = i - 312
            val v10 = mt[v9] xor ((mt[v9 + 1] xor mt[v9]) and 2147483647UL)
            mt[i] = mag(v10) xor (v10 shr 1) xor mt[i + -156]
        }
    }

    private fun g2() {
        for (i in 0 until 156) {
            val v9 = i + 312
            val v10 = mt[v9] xor ((mt[v9 + 1] xor mt[v9]) and 2147483647UL)
            mt[i] = mag(v10) xor (v10 shr 1) xor mt[i + 312 + 156]
        }
        for (i in 156 until 311) {
            val v11 = i + 312
            val v12 = mt[v11] xor ((mt[v11 + 1] xor mt[v11]) and 2147483647UL)
            mt[i] = mag(v12) xor (v12 shr 1) xor mt[i + -156]
        }
        val v13 = mt[623] xor ((mt[0] xor mt[623]) and 2147483647UL)
        mt[311] = mag(v13) xor (v13 shr 1) xor mt[155]
        mti = 0
    }

    fun generate(): ULong {
        if (mti == 312) g1() else if (624 <= mti) g2()
        val array = mt
        val num = mti
        mti = num + 1
        val x = array[num]
        val v12 = (((((x shr 29) and 22906492245UL) xor x) and 62583042209491UL) shl 17) xor ((x shr 29) and 22906492245UL) xor x
        return ((v12 and 18446744073709535095UL) shl 37) xor v12 xor ((((v12 and 18446744073709535095UL) shl 37) xor v12) shr 43)
    }

    companion object {

        fun generateKey(seed: ULong): ByteArray {
            val gen = MT19937(MT19937(seed).generate()).also { it.generate() }
            val key = ByteArray(4096)
            for (i in key.indices step 8) {
                val byteVal = gen.generate().swap().toByteArray()
                for (x in i until i + 8) {
                    key[x] = byteVal[x % 8]
                }
            }
            return key
        }

        private operator fun Int.plus(uLong: ULong) = toULong().plus(uLong)

        private fun ULong.swap() = (shr(32) or shl(32)).let { a ->
            (((a and 18446462603027742720UL) shr 16) or ((a and 281470681808895UL) shl 16)).let { b ->
                ((b and 18374966859414961920UL) shr 8) or ((b and 71777214294589695UL) shl 8)
            }
        }
    }
}
