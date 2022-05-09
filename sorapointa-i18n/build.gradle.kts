plugins {
    id("sorapointa-conventions")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(project(":sorapointa-utils"))
    implementation(KotlinX.serialization.json)
}
