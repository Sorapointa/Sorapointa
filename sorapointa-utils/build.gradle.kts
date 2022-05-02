plugins {
    id("sorapointa-conventions")
    kotlin("plugin.serialization")
}

version = "0.1.0-Dev"

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(KotlinX.serialization.json)
}
