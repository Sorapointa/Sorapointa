package org.sorapointa.dataloader

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassRefTypeSignature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import mu.KotlinLogging
import org.sorapointa.dataloader.ResourceHolder.loadAll
import org.sorapointa.utils.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

/**
 * ResourceHolder, store all DataLoader reference
 *
 * You can call [loadAll] to load all resources
 */
object ResourceHolder {
    private val dataMap = ConcurrentHashMap<String, DataLoader<Any>>()

    private val dataLoaderTypeName = DataLoader::class.java.canonicalName

    /**
     * Find field with type [DataLoader] in [org.sorapointa.dataloader] package
     * and initialize them
     * @return the count of successful initialized data loaders
     */
    fun findAndRegister(): Int {
        var count = 0
        ClassGraph()
            .enableClassInfo()
            .enableFieldInfo()
            .ignoreFieldVisibility()
            .acceptPackages("org.sorapointa.dataloader")
            .scan().allClasses.standardClasses
            .flatMap { classInfo ->
                classInfo.declaredFieldInfo
                    .filter {
                        (it.typeSignature as? ClassRefTypeSignature)?.fullyQualifiedClassName == dataLoaderTypeName
                    }
                    .map { fieldInfo ->
                        runCatching {
                            val clazz = classInfo.loadClass()
                            fieldInfo.loadClassAndGetField().apply {
                                isAccessible = true
                            }.get(clazz)
                        }.onSuccess {
                            count++
                        }.onFailure {
                            logger.warn(it) { "Failed to register data loader for '${fieldInfo.name}'" }
                        }.getOrNull()
                    }
            }
            .filterIsInstance<DataLoader<*>>()
            .forEach { it.init() }
        return count
    }

    /**
     * Load all registered data
     * @see [DataLoader.loadFromStream]
     */
    suspend fun loadAll() = withContext(Dispatchers.IO) {
        dataMap.map { (k, v) ->
            launch {
                val loaded = v.load()
                finalizeData(k, loaded)
            }
        }.joinAll()
    }

    internal fun registerData(dataLoader: DataLoader<Any>) {
        dataMap[dataLoader.path] = dataLoader
        logger.trace { "Data loader registered: path ${dataLoader.path}" }
    }

    @Suppress("MemberVisibilityCanBePrivate")
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
@Suppress("FunctionName")
inline fun <reified T : Any> DataLoader(
    path: String,
    context: CoroutineContext = Dispatchers.IO,
): DataLoader<T> =
    DataLoader(path, T::class, serializer(), context)

/**
 * Data loader, read json from file and deserialize
 *
 * @constructor internal, please use factory function above instead
 */
class DataLoader<T : Any> @PublishedApi internal constructor(
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
