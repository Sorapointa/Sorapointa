package org.sorapointa.dataloader

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import mu.KotlinLogging
import org.sorapointa.utils.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

object ResourceHolder {
    private val dataMap = ConcurrentHashMap<String, DataLoader<Any>>()

    private var parentJob = SupervisorJob()
    private val eventExceptionHandler =
        CoroutineExceptionHandler { _, e ->
            logger.error(e) { "Caught Exception on ResourceLoader" }
        }
    private var context =
        eventExceptionHandler + Dispatchers.Default + CoroutineName("ResourceLoader") + parentJob
    private var scope = CoroutineScope(context)

    fun init(parentScope: CoroutineScope = scope) {
        scope = parentScope
        parentJob = SupervisorJob(parentScope.coroutineContext[Job])
        context += parentJob
    }

    /**
     * Load all registered data
     * @param stream use stream to boost load, but probably went exception
     * @see [DataLoader.loadFromStream]
     */
    suspend fun loadAll(stream: Boolean = false) {
        dataMap.asSequence().asFlow().map { (k, v) ->
            scope.launch(context) {
                val loaded = if (stream) v.loadFromStream() else v.load()
                finalizeData(k, loaded)
            }
        }.collect { it.join() }
    }

    internal fun registerData(dataLoader: DataLoader<Any>) {
        dataMap[dataLoader.path] = dataLoader
        logger.trace { "Data loader registered: path ${dataLoader.path}" }
    }

    internal fun finalizeData(path: String, loaded: Any) {
        dataMap[path]?.apply {
            data = loaded
        } ?: run {
            logger.error { "Failed to finalize $path with data $loaded..." }
        }
    }
}

/**
 * Construct a [DataLoader]
 */
@OptIn(SorapointaInternal::class)
@Suppress("FunctionName")
inline fun <reified T : Any> DataLoader(
    path: String,
    context: CoroutineContext = Dispatchers.IO,
): DataLoader<T> =
    DataLoader<T>(path, T::class, serializer(), context).apply {
        init()
    }

/**
 * Data loader, read json from file and deserialize
 *
 * @constructor [SorapointaInternal], please use factory function above instead
 */
class DataLoader<T : Any> @SorapointaInternal constructor(
    path: String,
    private val clazz: KClass<T>,
    private val serializer: DeserializationStrategy<T>,
    private val context: CoroutineContext = Dispatchers.IO,
) {
    private val file = resolveResourceDirectory(path)

    fun init() {
        if (!file.exists()) throw NoSuchFileException(
            file = file,
            reason = "Failed to load data for class ${clazz.qualifiedOrSimple}"
        )
        ResourceHolder.registerData(this.uncheckedCast())
    }

    internal val path = file.absPath

    lateinit var data: T
        internal set

    /**
     * Load from file with buffer, then deserialize from string
     *
     * @see [Json.decodeFromString]
     * @see readTextBuffered
     */
    internal suspend fun load(): T = withContext(context) {
        val buffered = file.readTextBuffered()
        prettyJson.decodeFromString(serializer, buffered)
    }

    /**
     * Load from stream, better performance than [load]
     *
     * IO and deserialization processes are performed simultaneously
     *
     * **Experimental Api**
     * @see decodeFromStream
     */
    @OptIn(ExperimentalSerializationApi::class)
    internal suspend fun loadFromStream(): T = withContext(context) {
        file.inputStream().use {
            Json.decodeFromStream(serializer, it)
        }
    }
}
