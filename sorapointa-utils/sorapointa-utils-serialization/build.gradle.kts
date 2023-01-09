plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    kotlin("plugin.serialization")
}

dependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.kaml)
}
