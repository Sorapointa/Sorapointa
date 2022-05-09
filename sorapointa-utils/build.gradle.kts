plugins {
    id("sorapointa-conventions")
    kotlin("plugin.serialization")
}

allprojects {
    group = "org.sorapointa"
    version = "0.1.0-DEV"
}

dependencies {
    implementation(KotlinX.serialization.json)
}
