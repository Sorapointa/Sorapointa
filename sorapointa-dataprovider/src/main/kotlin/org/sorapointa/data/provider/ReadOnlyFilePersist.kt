package org.sorapointa.data.provider

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.sorapointa.utils.absPath
import org.sorapointa.utils.prettyJson
import org.sorapointa.utils.readTextBuffered
import org.sorapointa.utils.touch
import org.sorapointa.utils.writeTextBuffered
import java.io.File
import kotlin.reflect.full.createType

private val logger = mu.KotlinLogging.logger { }

/**
 * Read-only file persist
 * @param default the default value
 * @see FilePersist
 * @see SavableFilePersist
 */
@Suppress("UNCHECKED_CAST")
open class ReadOnlyFilePersist<T : Any>(
    override val location: File,
    default: T
) : SavableFilePersist<T> {
    protected val clazz = default::class

    protected val serializer: KSerializer<Any?> = serializer(clazz.createType())

    init {
        clazz.requireSerializable()
    }

    protected val mutex = Mutex()

    final override var data: T by atomic(default)

    override suspend fun reload(): T {
        location.run {
            logger.debug { "Reloading data from $absPath" }
            if (exists()) load() else save(data)
        }
        return data
    }

    override suspend fun save(data: T) = mutex.withLock {
        logger.debug { "Saving data $data" }
        location.touch()
        location.writeTextBuffered(prettyJson.encodeToString(serializer, data))
    }

    override suspend fun load(): T = mutex.withLock {
        val json = location.readTextBuffered()
        val t = (prettyJson.decodeFromString(serializer, json) as? T
            ?: error("Failed to cast Any? to ${clazz.qualifiedName}"))
        data = t
        return t.also {
            logger.debug { "Loaded data: $it" }
        }
    }
}
