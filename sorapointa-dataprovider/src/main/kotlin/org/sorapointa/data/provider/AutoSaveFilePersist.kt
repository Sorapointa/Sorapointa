package org.sorapointa.data.provider

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.sorapointa.utils.addShutdownHook
import java.io.File
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = mu.KotlinLogging.logger { }

/**
 * Auto Save File Persist
 * @param saveInterval delay how long to save file
 * @see [ReadOnlyFilePersist]
 * @see [FilePersist]
 */
open class AutoSaveFilePersist<T : Any>(
    override val location: File,
    default: T,
    private val saveInterval: Duration = 60.toDuration(DurationUnit.SECONDS),
) : ReadOnlyFilePersist<T>(location, default), SavableFilePersist<T> {
    private val scope = CoroutineScope(Dispatchers.IO)

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

    override suspend fun reload(): T {
        addShutdownHook {
            save(data)
        }
        launchSaveJob()
        return super.reload()
    }
}
