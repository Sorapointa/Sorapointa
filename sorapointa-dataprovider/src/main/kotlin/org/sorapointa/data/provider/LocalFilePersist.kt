package org.sorapointa.data.provider

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.sorapointa.utils.absPath
import org.sorapointa.utils.addShutdownHook
import org.sorapointa.utils.prettyJson
import org.sorapointa.utils.readTextBuffered
import org.sorapointa.utils.touch
import org.sorapointa.utils.writeTextBuffered
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.hasAnnotation
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = mu.KotlinLogging.logger { }

/**
 * Local file data persist storage
 * @property data must be annotated with @[Serializable]
 * @property location where you store the data
 * @see SavableFilePersist
 */
interface FilePersist<T : Any> {
    val data: T?

    val location: File

    /**
     * Reload data to memory
     *
     * Must be invoked at program start
     */
    suspend fun reload(): T

    suspend fun load(): T
}

/**
 * Savable file persist
 * @see FilePersist
 */
interface SavableFilePersist<T : Any> : FilePersist<T> {

    /**
     * Save data to disk
     *
     * Should be invoked after change, or data will be lost
     *
     * Some subclasses can automatically save data, for example [AutoSaveFilePersist]
     */
    suspend fun save(data: T)
}

private fun KClass<*>.isSerializable() =
    hasAnnotation<Serializable>() || hasAnnotation<Contextual>()

private fun KClass<*>.requireSerializable() {
    require(isSerializable()) { "Class $qualifiedName is not @Serializable or @Contextual" }
}

/**
 * Read-only file persist
 * @param default the default value
 * @see FilePersist
 */
@Suppress("UNCHECKED_CAST")
abstract class ReadOnlyFilePersist<T : Any>(
    override val location: File,
    default: T
) : SavableFilePersist<T> {
    protected val clazz = default::class

    protected val serializer: KSerializer<Any?> = serializer(clazz.createType())

    init {
        clazz.requireSerializable()
    }

    protected val mutex = Mutex()

    override var data: T = default
        protected set

    override suspend fun reload(): T {
        location.run {
            logger.debug { "Reloading data from $absPath" }
            if (exists()) load() else save(data)
        }
        return data
    }

    override suspend fun save(data: T) = mutex.withLock {
        logger.debug { "Saving data $data..." }
        location.touch()
        location.writeTextBuffered(prettyJson.encodeToString(serializer, data))
    }

    override suspend fun load(): T = mutex.withLock {
        val json = location.readTextBuffered()
        val t = (prettyJson.decodeFromString(serializer, json) as? T
            ?: error("Failed to cast Any? to ${clazz.qualifiedName}"))
        data = t
        return t
    }
}

/**
 * Auto Save File Persist
 */
abstract class AutoSaveFilePersist<T : Any>(
    override val location: File,
    default: T,
    private val saveInterval: Duration = 60.toDuration(DurationUnit.SECONDS),
) : ReadOnlyFilePersist<T>(location, default), SavableFilePersist<T> {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val saveJob: Job? = null

    init {
        addShutdownHook {
            save(data)
        }
    }

    private fun launchSaveJob() = scope.launch {
        logger.debug { "Launching Auto Save Job... Interval $saveInterval" }
        while (isActive) {
            delay(saveInterval.inWholeMicroseconds)
            save(data)
        }
    }

    override var data: T = default

    override suspend fun reload(): T {
        return super.reload()
    }
}
