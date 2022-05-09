package org.sorapointa.data.provider

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

/**
 * Local file data persist storage
 * @property data must be annotated with @[Serializable]
 * @property file where you store the data
 * @property scope coroutine will launch from this scope,
 * keep it same as caller side to make sure the structured concurrency has been followed
 */
interface FilePersist<T : Any> {

    val data: T?

    val file: File

    val scope: CoroutineScope

    /**
     * Initialize file persist
     * Must be called at program start
     */
    suspend fun init()

    /**
     * Load data from disk storage,
     * assign value to [data], then return it
     */
    suspend fun load(): T

    /**
     * Save data to disk
     *
     * Should be invoked after change, or data will be lost
     *
     * Some subclasses can automatically save data, for example [AutoSaveFilePersist]
     */
    suspend fun save(saveData: T = data!!)
}

internal fun KClass<*>.isSerializable() =
    hasAnnotation<Serializable>() || hasAnnotation<Contextual>()

internal fun KClass<*>.requireSerializable() {
    require(isSerializable()) { "Class $qualifiedName is not @Serializable or @Contextual" }
}
