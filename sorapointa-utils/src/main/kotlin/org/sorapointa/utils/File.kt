package org.sorapointa.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import kotlin.io.path.absolute
import kotlin.io.path.pathString

/**
 * Create a file and its parents
 */
suspend fun File.touch(): Boolean = withContext(Dispatchers.IO) {
    parentFile?.mkdirs()
    createNewFile()
}

val File.absPath
    get() = toPath().normalize().absolute().pathString

/**
 * Read text from the file with [Dispatchers.IO] context
 */
suspend fun File.readTextBuffered() = withContext(Dispatchers.IO) {
    inputStream().bufferedReader().use(BufferedReader::readText)
}

/**
 * Write text to the file with [Dispatchers.IO] context
 */
suspend fun File.writeTextBuffered(text: String) = withContext(Dispatchers.IO) {
    outputStream().bufferedWriter().use { it.write(text) }
}
