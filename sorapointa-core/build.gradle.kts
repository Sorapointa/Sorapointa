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
    implementation(libs.atomicfu)

    // network
    implementation(libs.netty)
    implementation(libs.kcp)
    // Ktor
    implementation(libs.ktor.server.wss)
    // Command
    api(libs.yac)
    // Console
    implementation(libs.jline)
    implementation(libs.password4j)

    testImplementation(project(":sorapointa-dispatch", "test"))
    testImplementation(project(":sorapointa-dataprovider", "test"))
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

application {
    applicationName = "sorapointa"
    mainClass.set("org.sorapointa.MainKt")
}
