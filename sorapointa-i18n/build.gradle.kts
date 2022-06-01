plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(KotlinX.serialization.json)
}
