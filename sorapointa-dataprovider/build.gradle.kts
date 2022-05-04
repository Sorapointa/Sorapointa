plugins {
    id("sorapointa-conventions")
}

version = "0.1.0-Dev"

dependencies {
    implementation(project(":sorapointa-utils"))

    implementation("org.jetbrains.kotlin:kotlin-reflect:_")

    implementation(KotlinX.serialization.json)
    implementation("org.jetbrains.kotlinx:atomicfu:_")
    implementation("org.litote.kmongo:kmongo-coroutine-serialization:_")
}
