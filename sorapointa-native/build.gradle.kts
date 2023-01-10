plugins {
    alias(libs.plugins.rust.wrapper)
}

rust {
    release.set(true)
    command.set("cargo")
    targets {
        this += defaultTarget()
    }
}
