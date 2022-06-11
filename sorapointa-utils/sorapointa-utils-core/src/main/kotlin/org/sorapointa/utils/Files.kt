package org.sorapointa.utils

import java.io.File
import kotlin.reflect.KClass

private val logger = mu.KotlinLogging.logger { }

/**
 * Global Work Directory, set by `user.dir`
 */
val globalWorkDirectory by lazy {
    val workDir = testDir ?: System.getProperty("user.dir") ?: error("Failed to get property 'user.dir'")

    File(workDir).also {
        logger.debug { "Global work directory: ${it.absPath}" }
    }
}

private val testDir by lazy {
    runCatching {
        Class.forName("org.sorapointa.config.TestConfig")
            .getDeclaredField("TEST_DIR")
            .get(null) as String
    }.getOrNull()
}

fun resolveHome(path: String): File? = System.getProperty("user.home")?.let { File(it, path) }

/**
 * Resolve work dir
 */
fun resolveWorkDirectory(path: String) = File(globalWorkDirectory, path)

fun resolveResourceDirectory(path: String) = File(resourceDirectory, path)

// Add all file you need here, like:
val configDirectory by lazy { resolveWorkDirectory("./config") }

val languagesDirectory by lazy { resolveWorkDirectory("./langs") }

val resourceDirectory by lazy { resolveWorkDirectory("./resources") }

/**
 * Extract languages files in resources (resources/langs) to [languagesDirectory]
 */
@SorapointaInternal
suspend fun extractLanguages(mainClazz: KClass<*>) {
    val source = jar(mainClazz.java)
    if (!languagesDirectory.exists() && source != null) {
        copyResourceDirectory(source, "langs", languagesDirectory)
    }
}
