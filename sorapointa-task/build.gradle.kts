@file:Suppress("GradlePackageUpdate")

plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(project(":sorapointa-utils:sorapointa-utils-core"))
    implementation(project(":sorapointa-utils:sorapointa-utils-time"))

    implementation(libs.bundles.log)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)

    implementation(libs.cron.utils)

    testImplementation(project(":sorapointa-dataprovider", "test"))
    testImplementation(libs.atomicfu)
}
