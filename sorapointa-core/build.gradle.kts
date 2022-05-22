@file:Suppress("GradlePackageUpdate")

plugins {
    id("sorapointa-conventions")
    application
}

dependencies {
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
    implementation("io.jpower.kcp:kcp-netty:_")
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
