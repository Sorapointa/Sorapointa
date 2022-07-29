package org.sorapointa.utils

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class I18nTest {
    private val dutchLocale get() = Locale.forLanguageTag("nl")

    @Test
    fun `test simple string lookup`() {
        assertEquals("This is a simple test", TestBundle.message("sora.test.simple"))
    }

    @Test
    fun `test simple string lookup with language override`() {
        assertEquals("Dit is een simpele test", TestBundle.message("sora.test.simple", locale = dutchLocale))
    }

    @Test
    fun `test string lookup for bundle that doesn't exist`() {
        assertEquals("This is a simple test", TestBundle.message("sora.test.simple", locale = Locale.ENGLISH))
    }

    @Test
    fun `test string lookup for string that doesn't exist in overridden locale bundle`() {
        assertEquals("This string is in default", TestBundle.message("sora.test.english.only", locale = dutchLocale))
    }

    @Test
    fun `test parameterized string lookup`() {
        assertEquals("This is a parameterized test 1 test", TestBundle.message("sora.test.parameterized", 1, "test"))
    }

    @Test
    fun `test available locales is equal to available bundles`() {
        assertEquals(TestBundle.availableLocales(), setOf(Locale.ENGLISH, dutchLocale)) // default locale is English
    }
}
