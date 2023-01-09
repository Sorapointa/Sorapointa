@file:Suppress("GradlePackageUpdate")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `sorapointa-conventions`
}

dependencies {
    implementation(libs.bundles.log)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":sorapointa-utils:sorapointa-utils-core"))
    implementation(project(":sorapointa-utils:sorapointa-utils-serialization"))
    implementation(libs.classgraph)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.apply {
        val newOptIn = listOf(
            "kotlinx.serialization.ExperimentalSerializationApi"
        )
        (OptInAnnotations.list + newOptIn).forEach {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=$it"
        }
    }
}
