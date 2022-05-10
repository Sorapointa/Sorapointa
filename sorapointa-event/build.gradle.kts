plugins {
    id("sorapointa-conventions")
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(KotlinX.serialization.json)
    implementation("org.jetbrains.kotlinx:atomicfu:_")
}
