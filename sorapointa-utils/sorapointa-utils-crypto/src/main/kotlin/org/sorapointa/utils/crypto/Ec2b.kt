@file:Suppress("unused")

package org.sorapointa.utils.crypto

import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sorapointa.utils.*
import java.io.File

private fun keyScramble(key: UByteArray) {
    val roundKeys = UByteArray(11 * 16)

    for (round in 0..10) {
        for (i in 0 until 16) {
            for (j in 0 until 16) {
                val idx = (round shl 8) + (i * 16) + j
                val roundIdx = round * 16 + i
                roundKeys[roundIdx] =
                    roundKeys[roundIdx] xor (aesXorpadTable[idx] xor stackTable[idx])
            }
        }
    }

    val chip = UByteArray(16)
    oqs128Encode(key, roundKeys, chip)
    memcpy(key, chip, 16)
}

private fun getDecryptVector(
    key: UByteArray,
    crypt: UByteArray,
): ByteArray {
    val outputSize = 4096
    val crypt64 = crypt.splitToULongArray()

    val var1 = crypt64.fold(0xFFFFFFFFFFFFFFFFuL) { acc, i ->
        acc xor i
    }

    val keyQword = key.splitToULongArray()

    val mt = MT19937(keyQword[1] xor 0xceac3b5a867837acUL xor var1 xor keyQword[0])

    val output64 = ULongArray(outputSize / 8) {
        mt.generate()
    }

    return output64.toLongArray().flatMap {
        it.toByteArray().toList()
    }.toByteArray()
}

class Ec2bData(
    val seed: ByteArray,
    val key: ByteArray
)

suspend fun Ec2bSeed.dumpToData() =
    Ec2bData(toByteArray(), decrypt())

class Ec2bSeed(
    private val keySize: UInt,
    private val decryptionKey: UByteArray,
    private val dataSize: UInt,
    private val data: UByteArray,
) {
    companion object {
        /**
         * Generate Ec2b seed and key
         */
        fun generate(keyLength: UInt = 16u, dataLength: UInt = 2048u): Ec2bSeed {
            val key = randomUByteArray(keyLength)
            val data = randomUByteArray(dataLength)
            return Ec2bSeed(keyLength, key, dataLength, data)
        }
    }

    /**
     * @return Ec2b Key Binary
     */
    suspend fun decrypt(): ByteArray = withContext(Dispatchers.Default) {
        val key = decryptionKey.copyOf()
        keyScramble(key)
        key.xor(keyXorTable)
        getDecryptVector(key, data)
    }

    /**
     * @return whether it is successfully saved
     */
    suspend fun decryptToFile(file: File, overwrite: Boolean = false): Boolean {
        if (file.exists()) {
            if (!overwrite) {
                return false
            } else file.delete()
        }
        val decrypted = decrypt()
        withContext(Dispatchers.IO) {
            file.touch()
            file.writeBytes(decrypted)
        }
        return true
    }

    /**
     * Build Ec2b seed to binary
     */
    fun toByteArray() = buildPacket {
        writeText("Ec2b")
        writeInt(keySize.toInt(), ByteOrder.LITTLE_ENDIAN)
        writeFully(decryptionKey)
        writeInt(dataSize.toInt(), ByteOrder.LITTLE_ENDIAN)
        writeFully(data)
    }.readBytes()
}

fun readEc2bOrNull(path: String): Ec2bSeed? = readEc2bOrNull(File(path))

fun readEc2bOrNull(file: File): Ec2bSeed? {
    if (!(file.exists() && file.isFile && file.canRead())) return null

    return readEc2b(file.readBytes())
}

fun readEc2b(byteArray: ByteArray): Ec2bSeed =
    buildPacket {
        writeFully(byteArray)
    }.use { pk ->
        pk.discard(4) // head, constantly "Ec2b"
        val keySize = pk.readIntLittleEndian().toUInt()
        val decryptionKey = pk.readBytes(keySize.toInt()).toUByteArray()
        val dataSize = pk.readIntLittleEndian().toUInt()
        val data: UByteArray = pk.readBytes(dataSize.toInt()).toUByteArray()
        Ec2bSeed(keySize, decryptionKey, dataSize, data)
    }
