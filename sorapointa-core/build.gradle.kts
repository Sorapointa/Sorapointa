@file:Suppress("GradlePackageUpdate")

plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    application
}

dependencies {
//    implementation(files("$projectDir/libs/kcp-netty-1.5.0.jar"))

    // Project submodules
    implementation(project(":sorapointa-dataloader"))
    implementation(project(":sorapointa-dispatch"))
    api(project(":sorapointa-dataprovider"))
    api(project(":sorapointa-event"))
    api(project(":sorapointa-i18n"))
    api(project(":sorapointa-proto"))
    api(project(":sorapointa-task"))
    api(project(":sorapointa-utils:sorapointa-utils-all"))

    // network
    implementation("io.netty:netty-handler:_")
    implementation("moe.sdl.kcp:kcp-netty:_")
    // Command
    api("moe.sdl.yac:core:_")
    // Console
    implementation("org.jline:jline:_")
}

configureLangsCopy()

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

application {
    applicationName = "sorapointa"
    mainClass.set("org.sorapointa.MainKt")
}
