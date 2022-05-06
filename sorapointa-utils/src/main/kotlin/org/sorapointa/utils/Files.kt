package org.sorapointa.utils

import java.io.File

private val logger = mu.KotlinLogging.logger { }

/**
 * Global Work Directory, set by `user.dir`
 */
val globalWorkDirectory by lazy {
    val workDir = System.getProperty("user.dir") ?: error("Failed to get property 'user.dir'")
    File(workDir).also {
        logger.debug { "Global work directory: ${it.absPath}" }
    }
}

/**
 * Resolve work dir
 */
fun resolveWorkDirectory(path: String) = File(globalWorkDirectory, path)

// Add all file you need here, like:
val configDirectory by lazy { resolveWorkDirectory("./config") }
