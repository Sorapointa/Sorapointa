package org.sorapointa.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.sorapointa.data.provider.DataFilePersist
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

private val logger = mu.KotlinLogging.logger { }

object I18nManager {
    // locale to i18n files
    internal val languageMap = ConcurrentHashMap<Locale, LanguagePack>()

    val supportedLanguages: List<Locale>
        get() = languageMap.keys().toList()

    fun registerLanguage(languagePack: LanguagePack) {
        val locale = languagePack.locale
        if (languageMap.containsKey(locale)) {
            logger.warn { "Language pack $locale already exists, overwriting it." }
        }
        languageMap[locale] = languagePack
    }

    /**
     * Register language pack from file
     * @param languageFile the file store [LanguagePack]
     */
    suspend fun registerLanguage(languageFile: File) {
        runCatching {
            if (!languageFile.exists()) throw NoSuchFileException(languageFile)
            val langPack = DataFilePersist(languageFile, LanguagePack.EMPTY).apply { init() }.data
            if (langPack == LanguagePack.EMPTY) {
                logger.error { "Failed to load language pack ${languageFile.absPath}" }
                return@runCatching
            }
            registerLanguage(langPack)
        }.onFailure {
            when (it) {
                is SerializationException ->
                    logger.error(it) { "Failed to register language file: ${languageFile.absPath}" }
                is NoSuchFileException ->
                    logger.error(it) { "Language pack file ${it.file.absPath} do not exist" }
                else -> throw it
            }
        }
    }

    // ends with .lang.json
    private val languageFileRegex by lazy {
        Regex("""^.+\.lang\.json$""", RegexOption.IGNORE_CASE)
    }

    /**
     * @param directory languages dir to register up
     * @param match regex for matching entire filename and extension
     * @param depth directory walk depth
     * @throws [IllegalStateException] when [directory] is empty or not exists
     */
    suspend fun registerLanguagesDirectory(
        directory: File,
        match: Regex = languageFileRegex,
        depth: Int = 1,
        context: CoroutineContext = Dispatchers.IO
    ) = withContext(context) {
        check(directory.exists()) { "Directory doesn't exist: ${directory.absPath}" }
        check(directory.isDirectory) { "File not a directory: ${directory.absPath}" }
        directory.walk().maxDepth(depth).asFlow()
            .filter(File::isFile)
            .filter { match.matches(it.name) }
            .map {
                launch { registerLanguage(it) }
            }.collect(::joinAll)
    }
}

/**
 * @property locale what language locale the pack is
 * @property strings language key to value
 * @see I18nManager
 */
@Serializable
data class LanguagePack(
    @Serializable(LocaleSerializer::class)
    val locale: Locale,

    // language key to value
    // Samples:
    // "command.help.desc" -> "A command for looking up command usage"
    // "command.help.notfound" -> "Input {0} not a validate command"
    val strings: Map<String, String>,
) {
    companion object {
        val EMPTY = LanguagePack(EMPTY_LOCALE, emptyMap())
    }
}

object I18nConfig : DataFilePersist<I18nConfig.Config>(
    File(configDirectory, "i18n.json"),
    Config()
) {
    @Serializable
    data class Config(
        @Serializable(LocaleSerializer::class)
        val globalLocale: Locale = DEFAULT_LOCALE,
    )
}

inline val globalLocale: Locale
    get() = I18nConfig.data.globalLocale

val DEFAULT_LOCALE: Locale = Locale.ENGLISH

val FALLBACK_LOCALE: Locale = Locale.ENGLISH

/**
 * @receiver i18n key, dot-styled path, like 'sorapointa.command.help.usage'
 * @param args all args to pass to placeholder
 * @return replaced string
 * @see I18nManager
 */
fun String.i18n(vararg args: Any?, locale: Locale? = null): String {
    val selected =
        I18nManager.supportedLanguages.byPriority(listOfNotNull(locale, globalLocale))
            ?: FALLBACK_LOCALE
    return I18nManager.languageMap[selected]?.strings
        ?.get(this)?.replaceWithOrder(*args) ?: run {
        logger.info { "Missing i18n value for key '$this', locale '$locale'" }
        this
    }
}

/**
 * An object with locale value
 *
 * @property locale the locale object holds
 */
interface LocaleAble {
    val locale: Locale?
}

fun String.i18n(vararg args: Any?, locale: LocaleAble?): String =
    i18n(args = args, locale = locale?.locale)
