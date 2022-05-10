group = "org.sorapointa"
version = "0.1.0-Dev"

plugins {
    kotlin("plugin.serialization") apply false
}

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
}

allprojects {
    group = "org.sorapointa"
    version = "0.1.0-DEV"
}
