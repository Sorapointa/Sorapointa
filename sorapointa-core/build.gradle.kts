@file:Suppress("GradlePackageUpdate")

plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    application
}

dependencies {
    // Project submodules
    implementation(project(":sorapointa-dataloader"))
    implementation(project(":sorapointa-dispatch"))
    api(project(":sorapointa-dataprovider"))
    api(project(":sorapointa-event"))
    api(project(":sorapointa-i18n"))
    api(project(":sorapointa-proto"))
    api(project(":sorapointa-task"))
    api(project(":sorapointa-utils:sorapointa-utils-all"))

    // KotlinX
    api("org.jetbrains.kotlinx:atomicfu:_")

    // network
    implementation("io.netty:netty-handler:_")
    implementation("moe.sdl.kcp:grasskcpper:_")
    // Ktor
    implementation("io.ktor:ktor-server-websockets:_")
    // Command
    api("moe.sdl.yac:core:_")
    // Console
    implementation("org.jline:jline:_")
    implementation("com.password4j:password4j:_")
    testImplementation(project(":sorapointa-dispatch", "test"))
    testImplementation(project(":sorapointa-dataprovider", "test"))
}

configureLangsCopy()

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

application {
    applicationName = "sorapointa"
    mainClass.set("org.sorapointa.MainKt")
}
