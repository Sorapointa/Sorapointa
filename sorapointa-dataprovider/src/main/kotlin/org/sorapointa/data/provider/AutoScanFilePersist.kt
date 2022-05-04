package org.sorapointa.data.provider

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = mu.KotlinLogging.logger { }

/**
 * Auto Save File Persist
 * @param scanInterval delay how long to scan from file
 * @see [ReadOnlyFilePersist]
 * @see [FilePersist]
 */
abstract class AutoScanFilePersist<T : Any>(
    override val location: File,
    default: T,
    private val scanInterval: Duration = 60.toDuration(DurationUnit.SECONDS),
) : ReadOnlyFilePersist<T>(location, default), SavableFilePersist<T> {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val scanJob: Job by lazy {
        scope.launch {
            logger.debug { "Launching Auto Scan Job... Interval $scanInterval" }
            while (isActive) {
                delay(scanInterval)
                load()
            }
        }
    }

    private fun launchScanJob() = scanJob

    override suspend fun reload(): T {
        launchScanJob()
        return super.reload()
    }
}
