plugins {
    id("sorapointa-conventions")
    kotlin("plugin.serialization")
}

version = "0.1.0-Dev"

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:_")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:_")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:_")
    implementation("io.ktor:ktor-server-call-logging-jvm:_")
    implementation("io.ktor:ktor-server-compression-jvm:_")
    implementation("io.ktor:ktor-server-netty-jvm:_")
    implementation("io.ktor:ktor-network-tls-certificates:_")
    implementation("io.ktor:ktor-server-html-builder:_")
    testImplementation("io.ktor:ktor-server-tests-jvm:_")
}
