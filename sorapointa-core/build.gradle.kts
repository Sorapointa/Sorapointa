plugins {
    id("sorapointa-conventions")
    application
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(project(":sorapointa-dispatch"))
    implementation(project(":sorapointa-event"))
    implementation(project(":sorapointa-i18n"))
    implementation(project(":sorapointa-kcp"))
    implementation(project(":sorapointa-proto"))
    implementation("moe.sdl.yac:core:_")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}
