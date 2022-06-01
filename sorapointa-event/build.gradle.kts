@file:Suppress("GradlePackageUpdate")

plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(KotlinX.serialization.json)
    implementation("org.jetbrains.kotlinx:atomicfu:_")
}
