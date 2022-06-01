@file:Suppress("GradlePackageUpdate")

plugins {
    `sorapointa-conventions`
}

dependencies {
    implementation(KotlinX.serialization.json)
    implementation("org.jetbrains.kotlinx:atomicfu:_")
}
