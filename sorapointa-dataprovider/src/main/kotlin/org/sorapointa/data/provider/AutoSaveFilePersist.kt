package org.sorapointa.data.provider

import kotlinx.coroutines.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import org.sorapointa.utils.addShutdownHook
import org.sorapointa.utils.prettyJson
import java.io.File
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = mu.KotlinLogging.logger { }

/**
 * Auto Save File Persist
 * @param saveInterval delay how long to save file
 * @see [DataFilePersist]
 * @see [FilePersist]
 */
open class AutoSaveFilePersist<T : Any>(
    file: File,
    default: T,
    serializer: KSerializer<T>,
    format: StringFormat = prettyJson,
    private val saveInterval: Duration = 60.toDuration(DurationUnit.SECONDS),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : DataFilePersist<T>(file, default, serializer, format, scope) {

    override suspend fun init() {
        addShutdownHook {
            save(data)
        }
        launchSaveJob()
        super.init()
    }

    private val saveJob: Job by lazy {
        scope.launch {
            logger.debug { "Launching Auto Save Job... Interval $saveInterval" }
            while (isActive) {
                delay(saveInterval)
                save(data)
            }
        }
    }

    private fun launchSaveJob() = saveJob
}
