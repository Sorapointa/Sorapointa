package org.sorapointa.data.provider

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.sorapointa.utils.*
import java.io.File
import kotlin.reflect.full.createType

private val logger = mu.KotlinLogging.logger { }

/**
 * Data file persist
 * @property file where you store the data
 * @param default the default value
 * @see FilePersist
 */
@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")
open class DataFilePersist<T : Any>(
    final override val file: File,
    default: T,
    final override val scope: CoroutineScope =
        ModuleScope("DataFilePersist", dispatcher = Dispatchers.IO)
) : FilePersist<T> {

    protected val clazz = default::class

    protected val serializer: KSerializer<Any?> = serializer(clazz.createType())

    protected val mutex = Mutex()

    @Suppress("PropertyName")
    @SorapointaInternal val _data = atomic(default)

    final override val data: T by _data

    init {
        clazz.requireSerializable()
    }

    inline fun updateData(update: (T) -> T) = _data.update(update)

    override suspend fun init(): Unit =
        withContext(scope.coroutineContext) {
            load()
            save()
        }

    suspend fun initAndLoad(): T =
        withContext(scope.coroutineContext) {
            load()
        }

    override suspend fun save(saveData: T) = mutex.withLock {
        withContext(scope.coroutineContext) {
            logger.debug { "Saving data $saveData" }
            file.touch()
            file.writeTextBuffered(prettyJson.encodeToString(serializer, saveData))
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
                val t = (
                    prettyJson.decodeFromString(serializer, json) as? T
                        ?: error("Failed to cast Any? to ${clazz.qualifiedOrSimple}")
                    )
                updateData { t }
                t.also {
                    logger.debug { "Loaded data: ${it::class.qualifiedOrSimple}" }
                    logger.trace { "Data content $it" }
                }
            }
        }
    }
}
