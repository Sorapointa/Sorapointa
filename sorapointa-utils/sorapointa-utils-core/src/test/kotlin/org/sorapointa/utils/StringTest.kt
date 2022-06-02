package org.sorapointa.utils

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StringTest {
    @Nested
    inner class ReplaceWithOrder {
        @Test
        fun `index start from 0`() {
            assertEquals("0 1 2", "{0} {1} {2}".replaceWithOrder("0", "1", "2"))
        }

        @Test
        fun `order matters`() {
            assertEquals("0 1", "{0} {1}".replaceWithOrder("0", "1"))
            assertEquals("1 0", "{1} {0}".replaceWithOrder("0", "1"))
        }

        @Test
        fun `ignore extra args`() {
            assertEquals("0", "{0}".replaceWithOrder("0", "1"))
        }

        @Test
        fun `ignore extra placeholder`() {
            assertEquals("0 {1}", "{0} {1}".replaceWithOrder("0"))
        }
    }
}
