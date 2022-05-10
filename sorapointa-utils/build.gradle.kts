plugins {
    id("sorapointa-conventions")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(KotlinX.serialization.json)
}
