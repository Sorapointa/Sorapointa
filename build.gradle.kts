import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") apply false
    java
    // NOT AN ERROR, see: https://youtrack.jetbrains.com/issue/KTIJ-19369
    // You can install a plugin to suppress it:
    // https://plugins.jetbrains.com/plugin/18949-gradle-libs-error-suppressor
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.abi.validator)
    alias(libs.plugins.wire) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    afterEvaluate {
        configureLogbackCopy()
    }
    configure<KtlintExtension> {
        disabledRules.set(setOf("no-wildcard-imports", "import-ordering"))
        filter {
            // exclude("**/generated/**")
            fun exclude(path: String) = exclude {
                projectDir.toURI().relativize(it.file.toURI()).normalize().path.contains(path)
            }
            setOf("/generated/", "/build/", "resources").forEach { exclude(it) }
        }
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
    group = "moe.sdl.sorapointa"
    version = "0.1.0-SNAPSHOT"
}

installGitHooks()
