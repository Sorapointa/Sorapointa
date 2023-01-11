import com.diffplug.gradle.spotless.FormatExtension

plugins {
    kotlin("jvm") apply false
    java
    // NOT AN ERROR, see: https://youtrack.jetbrains.com/issue/KTIJ-19369
    // You can install a plugin to suppress it:
    // https://plugins.jetbrains.com/plugin/18949-gradle-libs-error-suppressor
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.abi.validator)
    alias(libs.plugins.wire) apply false
    alias(libs.plugins.rust.wrapper) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    afterEvaluate {
        configureLogbackCopy()
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

spotless {
    fun FormatExtension.excludes() {
        targetExclude("**/build/", "**/generated/", "**/resources/")
    }

    fun FormatExtension.common() {
        trimTrailingWhitespace()
        lineEndings = com.diffplug.spotless.LineEnding.UNIX
        endWithNewline()
    }

    val ktlintConfig = mapOf(
        "ij_kotlin_allow_trailing_comma" to "true",
        "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
        "trailing-comma-on-declaration-site" to "true",
        "trailing-comma-on-call-site" to "true",
        "ktlint_disabled_rules" to "no-wildcard-imports, import-ordering, filename",
    )

    kotlin {
        target("**/*.kt")
        excludes()
        common()
        ktlint("0.47.1").editorConfigOverride(ktlintConfig)
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        excludes()
        common()
        ktlint("0.47.1").editorConfigOverride(ktlintConfig)
    }
}
