package org.sorapointa.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*
import java.util.Locale.LanguageRange

object LocaleSerializer : KSerializer<Locale> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Locale", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Locale = Locale.forLanguageTag(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Locale) = encoder.encodeString(value.toLanguageTag())
}

val EMPTY_LOCALE: Locale = Locale.forLanguageTag("empty")

fun Locale.toLanguageRange(weight: Double? = null): LanguageRange =
    weight?.let {
        LanguageRange(this.toLanguageTag(), weight)
    } ?: LanguageRange(this.toLanguageTag())

fun Collection<Locale>.toLanguageRanges(): Collection<LanguageRange> =
    map { it.toLanguageRange() }

fun List<Locale>.byPriority(localePriority: List<Locale>): Locale? =
    Locale.lookup(localePriority.toLanguageRanges().toList(), this)
