plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    testImplementation(project(":sorapointa-utils:sorapointa-utils-serialization"))
    implementation(KotlinX.serialization.json)
}
