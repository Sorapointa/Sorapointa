import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

group = "org.sorapointa"

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.github.microutils:kotlin-logging-jvm:_")
    implementation("ch.qos.logback:logback-classic:_")
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

