package org.sorapointa.utils

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.sorapointa.data.provider.DataFilePersist
import java.io.File
import java.util.*

internal val globalLocale: Locale get() = I18nConfig.data.globalLocale

internal val DEFAULT_LOCALE: Locale = Locale.ENGLISH

interface LocaleAble {
    val locale: Locale?
}

@SorapointaInternal
object I18nConfig : DataFilePersist<I18nConfig.Config>(
    File(configDirectory, "i18n.yaml"),
    Config(),
    Config.serializer(),
    lenientYaml,
) {
    @Serializable
    data class Config(
        @YamlComment(

            "Global locale setting",
            "",
            "We use RFC 1766 standard and two-letter language tag with IANA defined subtag.",
            "",
            "Sorapointa will find i18n string by order, Personal -> Global -> Fallback(i.e. English)",
            "When a language variant is not available, but main language is, fallback to main",
            "- Like: zh-Hant missing, so fallback to zh",
            "",
            "See more:",
            "- [Language tags in HTML and XML] https://www.w3.org/International/articles/language-tags/",
            "- [RFC 1766] https://datatracker.ietf.org/doc/html/rfc1766",
            "- [ISO 639] https://www.loc.gov/standards/iso639-2/php/code_list.php",
            "- [IANA Subtag Registry] https://www.iana.org/assignments/language-subtag-registry/",
        )
        @Serializable(LocaleSerializer::class)
        var globalLocale: Locale = DEFAULT_LOCALE,
    )
}

internal object LocaleSerializer : KSerializer<Locale> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Locale", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Locale = Locale.forLanguageTag(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Locale) = encoder.encodeString(value.toLanguageTag())
}
