package org.sorapointa.dataloder

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.serializer
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.utils.isCI
import kotlin.coroutines.CoroutineContext

/**
 * Data Loader for test, skip ci
 */
@Suppress("TestFunctionName")
inline fun <reified T : Any> TestDataLoader(
    path: String,
    context: CoroutineContext = Dispatchers.IO,
): DataLoader<T> =
    DataLoader(path, T::class, serializer(), context).apply {
        if (!isCI) init()
    }
