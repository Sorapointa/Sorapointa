package org.sorapointa.data.provider

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import org.sorapointa.utils.*
import java.io.File

private val logger = mu.KotlinLogging.logger { }

/**
 * Data file persist
 * @property file where you store the data
 * @param default the default value
 * @see FilePersist
 */
@Suppress("MemberVisibilityCanBePrivate")
open class DataFilePersist<T : Any>(
    final override val file: File,
    default: T,
    protected val serializer: KSerializer<T>,
    override val format: StringFormat = prettyJson,
    final override val scope: CoroutineScope =
        ModuleScope("DataFilePersist", dispatcher = Dispatchers.IO),
) : FilePersist<T> {
    protected val clazz = default::class

    protected val mutex = Mutex()

    @Volatile
    final override var data: T = default

    override suspend fun init(): Unit =
        withContext(scope.coroutineContext) {
            load()
        }

    suspend fun initAndLoad(): T =
        withContext(scope.coroutineContext) {
            load()
        }

    override suspend fun save(saveData: T) = mutex.withLock {
        withContext(scope.coroutineContext) {
            logger.debug { "Saving data $saveData" }
            file.touch()
            file.writeTextBuffered(format.encodeToString(serializer, saveData))
        }
    }

    override suspend fun load(): T {
        return withContext(scope.coroutineContext) {
            if (!file.exists()) {
                logger.debug { " ${file.absPath} does not exist, creating new default config..." }
                save(data)
            }
            mutex.withLock {
                val json = file.readTextBuffered()
                val t = format.decodeFromString(serializer, json)
                data = t
                t.also {
                    logger.debug { "Loaded data: ${it::class.qualifiedOrSimple}" }
                    logger.trace { "Data content $it" }
                }
            }
        }
    }
}
