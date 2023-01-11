package org.sorapointa.rust

import fr.stardustenterprises.yanl.NativeLoader
import java.util.concurrent.atomic.AtomicBoolean

private val loader = NativeLoader.Builder(root = "/org/sorapointa/rust").build()

private val loaded = AtomicBoolean(false)

/**
 * Load native library `spnative` if not load
 */
internal fun initRustLibrary() {
    if (!loaded.compareAndSet(false, true)) return
    loader.loadLibrary("spnative")
}
