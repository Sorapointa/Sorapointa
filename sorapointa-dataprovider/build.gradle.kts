plugins {
    id("sorapointa-conventions")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:_")

    implementation(KotlinX.serialization.json)
    implementation("org.jetbrains.kotlinx:atomicfu:_")
    api("org.litote.kmongo:kmongo-coroutine-serialization:_")
}
