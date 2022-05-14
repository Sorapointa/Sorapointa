plugins {
    id("sorapointa-conventions")
    application
}

dependencies {
    // Project submodules
    implementation(project(":sorapointa-dataprovider"))
    implementation(project(":sorapointa-dispatch"))
    implementation(project(":sorapointa-event"))
    implementation(project(":sorapointa-i18n"))
    implementation(project(":sorapointa-kcp"))
    implementation(project(":sorapointa-proto"))

    // Command
    implementation("moe.sdl.yac:core:_")
    // Console
    implementation("org.jline:jline:_")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

application {
    applicationName = "sorapointa"
    mainClass.set("org.sorapointa.MainKt")
}
