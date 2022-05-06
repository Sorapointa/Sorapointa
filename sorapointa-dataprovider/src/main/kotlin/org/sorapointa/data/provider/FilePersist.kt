package org.sorapointa.data.provider

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

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

    /**
     * load data, assign value to [data], then return it
     */
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

internal fun KClass<*>.isSerializable() =
    hasAnnotation<Serializable>() || hasAnnotation<Contextual>()

internal fun KClass<*>.requireSerializable() {
    require(isSerializable()) { "Class $qualifiedName is not @Serializable or @Contextual" }
}
