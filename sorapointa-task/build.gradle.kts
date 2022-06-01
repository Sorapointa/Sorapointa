@file:Suppress("GradlePackageUpdate")

plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))

    implementation(KotlinX.coroutines.core)
    implementation(KotlinX.serialization.core)

    implementation("com.cronutils:cron-utils:_")

    testImplementation(project(":sorapointa-dataprovider", "test"))
    testImplementation("org.jetbrains.kotlinx:atomicfu:_")
}
