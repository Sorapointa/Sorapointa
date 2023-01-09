plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(libs.kotlin.gradle.plugin) {
        exclude("com.squareup:kotlinpoet")
    }
    implementation("com.squareup:kotlinpoet:1.12.0")
    implementation("com.github.gmazzo:gradle-buildconfig-plugin:3.1.+")
    implementation("gradle.plugin.com.github.johnrengelman:shadow:7.1.+")
    implementation("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.18.5")
}

sourceSets {
    main {
        groovy {
            setSrcDirs(emptySet<File>()) // No Groovy
        }
        java {
            setSrcDirs(setOf("kotlin")) // No Java
        }
    }
    test {
        groovy {
            setSrcDirs(emptySet<File>())
        }
        java {
            setSrcDirs(setOf("kotlin"))
        }
    }
}

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(17))
    }
}
