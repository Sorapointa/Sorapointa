@file:Suppress("GradlePackageUpdate")

plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(project(":sorapointa-utils:sorapointa-utils-core"))
    implementation(project(":sorapointa-utils:sorapointa-utils-serialization"))

    implementation(libs.bundles.log)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.atomicfu)
}
