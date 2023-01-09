plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.build.kotlinpoet)
    implementation(libs.build.buildconfig)
    implementation(libs.build.shadow)
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
