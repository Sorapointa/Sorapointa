@file:Suppress("GradlePackageUpdate")

plugins {
    id("sorapointa-conventions")
}

dependencies {
    implementation(KotlinX.serialization.json)
    implementation("org.jetbrains.kotlinx:atomicfu:_")
}
