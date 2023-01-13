package org.sorapointa.rust.tokio

import java.io.Closeable

@Suppress("unused") // public api, native
class TokioHandle internal constructor(
    val nativePtr: Long,
) : Closeable, AutoCloseable {
    external fun isFinished(): Boolean

    external fun abort(): Boolean

    external fun await()

    external override fun close()
}
