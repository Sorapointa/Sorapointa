package org.sorapointa.data.provider

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.sorapointa.data.provider.DatabaseConfig.mutex
import org.sorapointa.utils.absPath
import org.sorapointa.utils.prettyJson
import org.sorapointa.utils.readTextBuffered
import org.sorapointa.utils.touch
import org.sorapointa.utils.writeTextBuffered
import java.io.File
import kotlin.reflect.full.createType

private val logger = mu.KotlinLogging.logger { }

/**
 * Data file persist
 * @property file where you store the data
 * @param default the default value
 * @see FilePersist
 */
@Suppress("UNCHECKED_CAST")
open class DataFilePersist<T : Any>(
    final override val file: File,
    default: T,
    final override val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : FilePersist<T> {

    protected val dataFilePersistExceptionHandler =
        CoroutineExceptionHandler { _, e -> logger.error(e) { "Caught Exception on DataFilePersist" } }
    protected val dataFilePersistContext = dataFilePersistExceptionHandler +
        Dispatchers.IO + CoroutineName("DataFilePersist")

    protected val clazz = default::class

    protected val serializer: KSerializer<Any?> = serializer(clazz.createType())

    protected val mutex = Mutex()

    final override var data: T by atomic(default)

    init {
        clazz.requireSerializable()
    }

    override suspend fun init(): Unit =
        withContext(dataFilePersistContext) {
            load()
        }

    suspend fun initAndLoad(): T =
        withContext(dataFilePersistContext) {
            load()
        }

    override suspend fun save(data: T) = mutex.withLock {
        withContext(dataFilePersistContext) {
            logger.debug { "Saving data $data" }
            file.touch()
            file.writeTextBuffered(prettyJson.encodeToString(serializer, data))
        }
    }

    override suspend fun load(): T {
        return withContext(dataFilePersistContext) {
            if (!file.exists()) {
                logger.debug { " ${file.absPath} does not exist, creating new default config..." }
                save(data)
            }
            mutex.withLock {
                val json = file.readTextBuffered()
                val t = (
                    prettyJson.decodeFromString(serializer, json) as? T
                        ?: error("Failed to cast Any? to ${clazz.qualifiedName}")
                    )
                data = t
                t.also {
                    logger.debug { "Loaded data: $it" }
                }
            }
        }
    }
}
