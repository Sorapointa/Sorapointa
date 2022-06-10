plugins {
    kotlin("plugin.serialization") apply false
    kotlin("jvm") apply false
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    afterEvaluate {
        configureLogbackCopy()
    }
}

allprojects {
    group = "moe.sdl.sorapointa"
    version = "0.1.0-SNAPSHOT"
}
