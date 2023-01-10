plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(project(":sorapointa-utils:sorapointa-utils-core"))
    implementation(project(":sorapointa-utils:sorapointa-utils-serialization"))
    implementation(project(":sorapointa-utils:sorapointa-utils-crypto"))
}
