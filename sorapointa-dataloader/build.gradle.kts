plugins {
    id("sorapointa-conventions")
}

version = "0.1.0-Dev"

dependencies {
    implementation(project(":sorapointa-utils"))
    implementation(KotlinX.serialization.json)
    implementation("org.jetbrains.kotlinx:atomicfu:_")
}
