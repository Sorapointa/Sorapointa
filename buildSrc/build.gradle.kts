plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.aliyun.com/repository/gradle-plugin")

    gradlePluginPortal()
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    implementation("com.github.gmazzo:gradle-buildconfig-plugin:3.0.3")
    implementation("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")
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
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}
