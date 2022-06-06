@file:Suppress("GradlePackageUpdate")

plugins {
    `sorapointa-conventions`
}

dependencies {
    implementation(project(":sorapointa-utils:sorapointa-utils-serialization"))

    implementation(KotlinX.serialization.json)
    implementation("org.jetbrains.kotlinx:atomicfu:_")
    implementation("io.github.classgraph:classgraph:_")
}
