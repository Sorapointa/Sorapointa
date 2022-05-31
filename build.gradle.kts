plugins {
    kotlin("plugin.serialization") apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
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
    apply(plugin = "com.github.johnrengelman.shadow")
}

subprojects.apply {
    configureLogbackCopy()
}

allprojects {
    group = "org.sorapointa"
    version = "0.1.0-DEV"
}
