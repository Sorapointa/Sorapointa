@file:Suppress("GradlePackageUpdate")

plugins {
    id("sorapointa-conventions")
    application
}

dependencies {
//    implementation(files("$projectDir/libs/kcp-netty-1.5.0.jar"))

    // Project submodules
    implementation(project(":sorapointa-dataloader"))
    implementation(project(":sorapointa-dataprovider"))
    implementation(project(":sorapointa-dispatch"))
    implementation(project(":sorapointa-event"))
    implementation(project(":sorapointa-i18n"))
    implementation(project(":sorapointa-proto"))
    implementation(project(":sorapointa-task"))
    implementation(project(":sorapointa-utils"))

    // network
    implementation("io.netty:netty-handler:_")
    implementation("moe.sdl.kcp:kcp-netty:_")
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
