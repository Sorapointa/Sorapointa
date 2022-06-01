@file:Suppress("GradlePackageUpdate")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.protobuf.gradle.*

plugins {
    id("sorapointa-conventions")
    id("com.google.protobuf")
    idea
}

val prop = getRootProjectLocalProps()

dependencies {
    api("com.google.protobuf:protobuf-java:_")
    api("com.google.protobuf:protobuf-kotlin:_")
    api("io.ktor:ktor-utils:_")
}

protobuf {
    generatedFilesBaseDir = "$projectDir/src/generated/"

    generateProtoTasks {
        ofSourceSet("main").forEach { task ->
            task.builtins {
                id("kotlin") {}
            }
            if (prop["proto.fullCompile"] == "true") {
                task.doFirst {
                    delete(task.outputs)
                }
            }
        }
    }
}

sourceSets {
    main {
        proto {
            setSrcDirs(setOf("src/proto"))
        }
        java {
            srcDir("src/generated")
        }
    }
}

idea {
    module {
        sourceDirs.plus(file("src/proto"))
    }
}
