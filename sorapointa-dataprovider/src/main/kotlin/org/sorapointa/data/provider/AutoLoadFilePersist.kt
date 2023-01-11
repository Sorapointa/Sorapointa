package org.sorapointa.data.provider

import kotlinx.coroutines.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import org.sorapointa.utils.prettyJson
import java.io.File
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = mu.KotlinLogging.logger { }

/**
 * Auto Load File Persist
 * @param scanInterval delay how long to scan from file
 * @see [DataFilePersist]
 * @see [FilePersist]
 */
open class AutoLoadFilePersist<T : Any>(
    file: File,
    default: T,
    serializer: KSerializer<T>,
    format: StringFormat = prettyJson,
    private val scanInterval: Duration = 60.toDuration(DurationUnit.SECONDS),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : DataFilePersist<T>(file, default, serializer, format, scope) {

    override suspend fun init() {
        launchScanJob()
        super.init()
    }

    private val scanJob: Job by lazy {
        scope.launch {
            logger.debug { "Launching Auto Load Job... Interval $scanInterval" }
            while (isActive) {
                delay(scanInterval)
                load()
            }
        }
    }

    private fun launchScanJob() = scanJob
}
