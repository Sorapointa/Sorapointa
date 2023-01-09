plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    kotlin("plugin.serialization")
}

dependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.datetime)
    implementation(libs.ktor.utils)
}
