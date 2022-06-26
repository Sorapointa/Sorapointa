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
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import org.sorapointa.data.provider.DataFilePersist
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

private val logger = mu.KotlinLogging.logger { }

// ends with .lang.yaml
private val languageFileRegex by lazy {
    Regex("""^.+\.lang\.yaml$""", RegexOption.IGNORE_CASE)
}

interface I18nIManager {

    fun registerLanguage(languagePack: LanguagePack)

    suspend fun registerLanguage(languageFile: File)

    suspend fun registerLanguagesDirectory(
        directory: File,
        match: Regex = languageFileRegex,
        depth: Int = 1,
        context: CoroutineContext = Dispatchers.IO
    )
}

object I18nManager : I18nIManager {
    // locale to i18n files
    internal val languageMap = ConcurrentHashMap<Locale, LanguagePack>()

    val supportedLanguages: List<Locale>
        get() = languageMap.keys().toList()

    override fun registerLanguage(languagePack: LanguagePack) {
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
    override suspend fun registerLanguage(languageFile: File) {
        runCatching {
            if (!languageFile.exists()) throw NoSuchFileException(languageFile)
            val langPack = DataFilePersist(languageFile, LanguagePack.EMPTY, format = Yaml).apply { init() }.data
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

    /**
     * @param directory languages dir to register up
     * @param match regex for matching entire filename and extension
     * @param depth directory walk depth
     * @throws [IllegalStateException] when [directory] is empty or not exists
     */
    override suspend fun registerLanguagesDirectory(
        directory: File,
        match: Regex,
        depth: Int,
        context: CoroutineContext
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

@SorapointaInternal
object I18nConfig : DataFilePersist<I18nConfig.Config>(
    File(configDirectory, "i18n.yaml"),
    Config(),
    format = Yaml,
) {
    @Serializable
    data class Config(
        @Comment(
            """
            Global locale setting
            
            We use RFC 1766 standard and two-letter language tag with IANA defined subtag.
            
            Sorapointa will find i18n string by order, Personal -> Global -> Fallback(i.e. English)
            When a language variant is not available, but main language is, fallback to main 
            - Like: zh-Hant missing, so fallback to zh
            
            See more: 
            - [Language tags in HTML and XML] https://www.w3.org/International/articles/language-tags/
            - [RFC 1766] https://datatracker.ietf.org/doc/html/rfc1766
            - [ISO 639] https://www.loc.gov/standards/iso639-2/php/code_list.php
            - [IANA Subtag Registry] https://www.iana.org/assignments/language-subtag-registry/
        """
        )
        @Serializable(LocaleSerializer::class)
        var globalLocale: Locale = DEFAULT_LOCALE,
    )
}

inline val globalLocale: Locale
    get() = I18nConfig.data.globalLocale

internal val DEFAULT_LOCALE: Locale = Locale.ENGLISH

internal val FALLBACK_LOCALE: Locale = Locale.ENGLISH

/**
 * @receiver i18n key, dot-styled path, like 'sorapointa.command.help.usage'
 * @param args all args to pass to placeholder
 * @return replaced string
 * @see I18nManager
 */
fun String.i18n(vararg args: Any?, locale: Locale? = null): String {
    val selected =
        I18nManager.supportedLanguages.byPriority(listOfNotNull(locale, globalLocale, FALLBACK_LOCALE))

    // Add more information for writing language file
    if (logger.isDebugEnabled) {
        if (locale != null && I18nManager.languageMap[locale] == null) {
            logger.debug { "Missing i18n user locale $locale for key '$this'" }
        }
        if (I18nManager.languageMap[globalLocale] == null) {
            logger.debug { "Missing i18n global locale $globalLocale for key '$this'" }
        }
        if (FALLBACK_LOCALE != globalLocale && I18nManager.languageMap[FALLBACK_LOCALE] == null) {
            logger.debug { "Missing i18n fallback locale $globalLocale for key '$this'" }
        }
    }

    return I18nManager.languageMap[selected]?.strings?.get(this)?.replaceWithOrder(*args) ?: this
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
