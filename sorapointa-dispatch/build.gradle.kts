@file:Suppress("GradlePackageUpdate")

plugins {
    kotlin("plugin.serialization")
    application
    `sorapointa-conventions`
    `sorapointa-publish`
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(project(":sorapointa-i18n"))
    implementation(project(":sorapointa-event"))
    implementation(project(":sorapointa-proto"))
    implementation(project(":sorapointa-utils:sorapointa-utils-all"))

    api(libs.bundles.log)
    api(libs.kotlinx.coroutines.core)

    api(libs.ktor.server.core)
    api(libs.ktor.server.content.negotiation)
    api(libs.ktor.server.logging)
    api(libs.ktor.server.compression)
    api(libs.ktor.server.netty)
    api(libs.ktor.server.status.page)
    api(libs.ktor.server.html)

    api(libs.ktor.client.core)
    api(libs.ktor.client.cio)
    api(libs.ktor.client.logging)
    api(libs.ktor.client.content.negotiation)

    api(libs.ktor.network.cert)
    api(libs.ktor.serialization.kotlinx.json)

    implementation(libs.password4j)
    implementation(libs.atomicfu)

    testImplementation(libs.ktor.server.tests)
    testImplementation(project(":sorapointa-dataprovider", "test"))
}

application {
    applicationName = "sorapointa-dispatch"
    mainClass.set("org.sorapointa.dispatch.DispatchServerKt")
}
