package org.sorapointa.rust

import fr.stardustenterprises.yanl.NativeLoader

object Native {

    init {
        val loader = NativeLoader.Builder(
            root = "/org/sorapointa/rust"
        ).build()
        loader.loadLibrary("spnative")
    }

    external fun rustNative()
}
