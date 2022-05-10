plugins {
    id("sorapointa-conventions")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(KotlinX.serialization.json)
}
