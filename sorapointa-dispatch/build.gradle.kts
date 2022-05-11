plugins {
    id("sorapointa-conventions")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(project(":sorapointa-i18n"))
    implementation(project(":sorapointa-event"))
    implementation(project(":sorapointa-utils"))
    implementation(project(":sorapointa-proto"))
    implementation("io.ktor:ktor-server-core-jvm:_")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:_")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:_")
    implementation("io.ktor:ktor-server-call-logging-jvm:_")
    implementation("io.ktor:ktor-server-compression-jvm:_")
    implementation("io.ktor:ktor-server-netty-jvm:_")
    implementation("io.ktor:ktor-server-status-pages:_")
    implementation("io.ktor:ktor-network-tls-certificates:_")
    implementation("io.ktor:ktor-server-html-builder:_")
    testImplementation("io.ktor:ktor-server-tests-jvm:_")
    implementation("io.ktor:ktor-client-core:_")
    implementation("io.ktor:ktor-client-cio:_")
    implementation("io.ktor:ktor-client-logging:_")
    implementation("io.ktor:ktor-client-content-negotiation:_")
}
