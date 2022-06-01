plugins {
    kotlin("plugin.serialization") apply false
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

if (JavaVersion.current() != JavaVersion.VERSION_17) {
    throw GradleException("Sorapointa requires JDK 17 to build and develop, current version: ${JavaVersion.current()}")
}

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
}

subprojects.apply {
    configureLogbackCopy()
}

allprojects {
    group = "org.sorapointa"
    version = "0.1.0-DEV"
}
