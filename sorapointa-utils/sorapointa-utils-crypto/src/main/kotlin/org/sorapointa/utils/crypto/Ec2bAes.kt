package org.sorapointa.utils.crypto

private fun xorr(a: UByteArray, b: UByteArray) {
    val n = 16 // 16 rounds
    var i = 0
    while (i < n) {
        a[i] = a[i] xor b[i]
        i++
    }
}

private fun xorRoundKey(state: UByteArray, keys: UByteArray, round: Int) {
    xorr(state, keys.copyOfRange(round * 16, (round + 1) * 16))
}

private fun subBytesInv(a: UByteArray) {
    val n = 16 // 16 rounds
    for (i in 0 until n) {
        a[i] = lookupSBoxInv[a[i].toInt()]
    }
}

private fun shiftRowsInv(state: UByteArray) {
    val temp = UByteArray(16)
    memcpy(temp, state, 16)

    for (i in 0 until 16) {
        state[i] = temp[shiftRowsTableInv[i].toInt()]
    }
}

// Perform the inverse mix columns matrix on one column of 4 bytes
// http://en.wikipedia.org/wiki/Rijndael_mix_columns
private fun mixColInv(state: UByteArray, start: Int) {
    val (a0, a1, a2, a3) = state.copyOfRange(start, start + 4).map { it.toInt() }
    state[start] = lookupG14[a0] xor lookupG9[a3] xor lookupG13[a2] xor lookupG11[a1]
    state[start + 1] = lookupG14[a1] xor lookupG9[a0] xor lookupG13[a3] xor lookupG11[a2]
    state[start + 2] = lookupG14[a2] xor lookupG9[a1] xor lookupG13[a0] xor lookupG11[a3]
    state[start + 3] = lookupG14[a3] xor lookupG9[a2] xor lookupG13[a1] xor lookupG11[a0]
}

private fun mixColsInv(state: UByteArray) {
    mixColInv(state, 0)
    mixColInv(state, 4)
    mixColInv(state, 8)
    mixColInv(state, 12)
}

internal fun memcpy(dest: UByteArray, src: UByteArray, size: Int) {
    src.copyOfRange(0, size).forEachIndexed { idx, value ->
        dest[idx] = value
    }
}

internal fun oqs128Encode(plainText: UByteArray, schedule: UByteArray, cipherText: UByteArray) {
    // First Round
    memcpy(cipherText, plainText, 16)
    xorRoundKey(cipherText, schedule, 0)

    // Middle Rounds
    for (rounds in 0 until 9) {
        subBytesInv(cipherText)
        shiftRowsInv(cipherText)
        mixColsInv(cipherText)
        xorRoundKey(cipherText, schedule, rounds + 1)
    }

    // Final Round
    subBytesInv(cipherText)
    shiftRowsInv(cipherText)
    xorRoundKey(cipherText, schedule, 10)
}
