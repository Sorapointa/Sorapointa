package org.sorapointa.utils

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class LocalSerializerTest {
    @Test
    fun `serialize and deserialize`() {
        val origLocale = Locale.CHINA
        val encoded = prettyJson.encodeToString(LocaleSerializer, origLocale)
        val decoded = prettyJson.decodeFromString(LocaleSerializer, encoded)
        assertEquals(origLocale, decoded)
    }
}
