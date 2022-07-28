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

    api("io.ktor:ktor-server-core-jvm:_")
    api("io.ktor:ktor-server-content-negotiation-jvm:_")
    api("io.ktor:ktor-server-call-logging-jvm:_")
    api("io.ktor:ktor-server-compression-jvm:_")
    api("io.ktor:ktor-server-netty-jvm:_")
    api("io.ktor:ktor-server-status-pages:_")
    api("io.ktor:ktor-server-html-builder:_")

    api("io.ktor:ktor-client-core:_")
    api("io.ktor:ktor-client-cio:_")
    api("io.ktor:ktor-client-logging:_")
    api("io.ktor:ktor-client-content-negotiation:_")

    api("io.ktor:ktor-network-tls-certificates:_")
    api("io.ktor:ktor-serialization-kotlinx-json:_")

    implementation("com.password4j:password4j:_")

    api("io.ktor:ktor-server-tests-jvm:_")
    api(project(":sorapointa-dataprovider", "test"))
    api("org.jetbrains.kotlinx:atomicfu:_")
}

application {
    applicationName = "sorapointa-dispatch"
    mainClass.set("org.sorapointa.dispatch.DispatchServerKt")
}
