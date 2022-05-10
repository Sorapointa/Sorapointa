package org.sorapointa.task

import com.cronutils.model.Cron
import com.cronutils.model.CronType.UNIX
import com.cronutils.model.definition.CronDefinition
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.sorapointa.utils.now
import org.sorapointa.utils.toZonedUtc
import org.sorapointa.utils.unwrap

private val logger = mu.KotlinLogging.logger { }

/**
 * Cron definition, UNIX-style
 * @see <a href="https://en.wikipedia.org/wiki/Cron">Wikipedia - Cron</a>
 */
private val cronDefinition: CronDefinition by lazy {
    CronDefinitionBuilder.instanceDefinitionFor(UNIX)
}

private val parser by lazy { CronParser(cronDefinition) }

/**
 * Parse UNIX-styled Cron String
 * @return [Cron] on success, null on failure
 * @see cronDefinition
 */
fun parseCron(cron: String): Cron = parser.parse(cron)

/**
 * Variety of [parseCron], return null when failed to parse
 */
fun parseCronOrNull(cron: String): Cron? = runCatching {
    parseCron(cron)
}.onFailure {
    logger.warn(it) { "Failed to parse cron expression $cron" }
}.getOrNull()

/**
 * Next execution time from [time] of [Cron] expression
 */
fun Cron.nextExecutionTime(time: Instant = now()): Instant? {
    val executionTime = ExecutionTime.forCron(this)
    return executionTime.nextExecution(time.toZonedUtc()).unwrap()
        ?.toInstant()?.toKotlinInstant()
}

/**
 * Last execution time from [time] of [Cron] expression
 */
fun Cron.lastExecutionTime(time: Instant = now()): Instant? {
    val executionTime = ExecutionTime.forCron(this)
    return executionTime.lastExecution(time.toZonedUtc()).unwrap()
        ?.toInstant()?.toKotlinInstant()
}

@JvmInline
@Serializable
value class CronWrapper(
    @Serializable(CronSerializer::class)
    val cron: Cron,
)

fun Cron.wrap() = CronWrapper(this)

object CronSerializer : KSerializer<Cron> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Cron", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Cron {
        val cron = decoder.decodeString()
        return runCatching { parseCron(cron) }
            .getOrElse { throw SerializationException("Failed to parse cron expression $cron", it) }
    }

    override fun serialize(encoder: Encoder, value: Cron) = encoder.encodeString(value.asString())
}
