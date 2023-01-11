package org.sorapointa.rust.logging

import ch.qos.logback.classic.Logger
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import org.sorapointa.rust.initRustLibrary

@Suppress("UNUSED")
internal object RustLogger {
    init {
        initRustLibrary()
    }

    @JvmStatic
    private val logger = KotlinLogging.logger("SP-Native")

    /**
     * Setup function, Can only be called exactly once,
     * or it will throw [IllegalStateException]
     */
    fun setup() {
        val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        val levelInt = when (rootLogger.level.levelStr) {
            "OFF" -> 1 // Rust `log` crate has no `OFF` level
            "ERROR" -> 1
            "WARN" -> 2
            "INFO" -> 3
            "DEBUG" -> 4
            "TRACE" -> 5
            else -> 3 // Unknown, default to `INFO`
        }
        setup(levelInt)
    }

    external fun setup(level: Int)

    @JvmStatic
    fun error(message: String) = logger.error(message)

    @JvmStatic
    fun warn(message: String) = logger.warn(message)

    @JvmStatic
    fun info(message: String) = logger.info(message)

    @JvmStatic
    fun debug(message: String) = logger.debug(message)

    @JvmStatic
    fun trace(message: String) = logger.trace(message)
}
