package org.sorapointa.logger

import mu.KotlinLogging
import org.junit.jupiter.api.Test

private val logger = KotlinLogging.logger {}

class LogTest {
    @Test
    fun log() {
        logger.error { "Lorem ipsum dolor sit amet, consectetur adipiscing elit." }
        logger.warn { "Maecenas malesuada tellus id est sodales, sit amet elementum sapien consectetur." }
        logger.info { "Maecenas efficitur massa eget mattis fringilla." }
        logger.debug { "Ut at eros sit amet neque venenatis sollicitudin et id risus!" }
        logger.trace { "Sed in libero pretium, luctus nunc sit amet, euismod ante." }
    }

    @Test
    fun logWithExceptions() {
        logger.error(StackOverflowError()) { "Lorem ipsum dolor sit amet, consectetur adipiscing elit." }
        logger.warn(OutOfMemoryError()) { "Cras a ex id ipsum sagittis porttitor." }
        logger.info(IndexOutOfBoundsException()) { "Curabitur at diam eu nibh hendrerit mollis." }
        logger.debug(IllegalStateException()) { "Aenean quis libero aliquet metus viverra hendrerit." }
        logger.trace(IllegalArgumentException()) { "Donec dignissim sem non ipsum suscipit, sit amet fermentum nunc iaculis." }
    }
}
