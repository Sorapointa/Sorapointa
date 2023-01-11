package org.sorapointa.rust.logging

import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

object LoggerTest {
    @Test
    fun `setup twice should throw exception`() {
        RustLogger.setup()
        assertFailsWith<IllegalStateException> {
            RustLogger.setup()
        }
    }
}
