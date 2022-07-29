@file:Suppress("GradlePackageUpdate")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `sorapointa-conventions`
}

dependencies {
    implementation(project(":sorapointa-utils:sorapointa-utils-serialization"))

    implementation(KotlinX.serialization.json)
    implementation("org.jetbrains.kotlinx:atomicfu:_")
    implementation("io.github.classgraph:classgraph:_")
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
