package org.sorapointa.data.provider

import kotlinx.coroutines.*
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
    private val scanInterval: Duration = 60.toDuration(DurationUnit.SECONDS),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : DataFilePersist<T>(file, default, scope) {

    override suspend fun init() {
        launchScanJob()
        super.init()
    }

    private val scanJob: Job by lazy {
        scope.launch(dataFilePersistContext) {
            logger.debug { "Launching Auto Load Job... Interval $scanInterval" }
            while (isActive) {
                delay(scanInterval)
                load()
            }
        }
    }

    private fun launchScanJob() = scanJob


}
