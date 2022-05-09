package org.sorapointa.utils

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class I18nTest {
    @BeforeEach
    fun reloadI18nConfig(): Unit = runBlocking {
        I18nManager.languageMap.clear()
        I18nConfig.init()
    }

    @Test
    fun `placeholder should be replaced`() = runBlocking {
        I18nManager.registerLanguage(
            LanguagePack(
                Locale.ENGLISH,
                strings = mapOf(
                    "sora.test.placeholder" to "Test {0} {1}"
                )
            ),
        )
        assertEquals("Test 1 2", "sora.test.placeholder".i18n("1", "2", locale = Locale.ENGLISH))
    }

    @Test
    fun lookupString() {
        I18nManager.registerLanguage(
            LanguagePack(
                Locale.ENGLISH,
                strings = mapOf(
                    "sora.test" to "Test"
                )
            ),
        )
        assertEquals("Test", "sora.test".i18n(locale = Locale.ENGLISH))
    }

    @Test
    fun `variants fallback to main`() {
        I18nManager.registerLanguage(
            LanguagePack(
                Locale.CHINESE,
                strings = mapOf(
                    "sora.test.fallback.variant" to "测试"
                )
            ),
        )
        assertEquals("测试", "sora.test.fallback.variant".i18n(locale = Locale.CHINA))
    }

    @Test
    fun `fallback to global`() {
        val testGlobal = Locale.ITALIAN

        I18nConfig.data = I18nConfig.Config(testGlobal)
        I18nManager.registerLanguage(
            LanguagePack(
                testGlobal,
                strings = mapOf(
                    "sora.test.fallback.global" to "global"
                )
            ),
        )

        assertEquals("global", "sora.test.fallback.global".i18n(locale = Locale.CHINA))
    }

    @Test
    fun `fallback to default`() {
        val testGlobal = Locale.ITALIAN

        I18nConfig.data = I18nConfig.Config(testGlobal)
        I18nManager.registerLanguage(
            LanguagePack(
                DEFAULT_LOCALE,
                strings = mapOf(
                    "sora.test.fallback.default" to "default"
                )
            ),
        )

        assertEquals("default", "sora.test.fallback.default".i18n(locale = Locale.CHINA))
    }
}
