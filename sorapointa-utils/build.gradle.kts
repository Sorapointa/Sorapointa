plugins {
    id("sorapointa-conventions")
    kotlin("plugin.serialization")
}

version = "0.1.0-Dev"

dependencies {
    implementation(KotlinX.serialization.json)
}
