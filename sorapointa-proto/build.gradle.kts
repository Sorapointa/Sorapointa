@file:Suppress("GradlePackageUpdate")

import com.google.protobuf.gradle.*

plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    id("com.google.protobuf")
    idea
}

val prop = getRootProjectLocalProps()

ext["no-utils"] = true

dependencies {
    api("com.google.protobuf:protobuf-java:_")
    api("com.google.protobuf:protobuf-kotlin:_")
    api(Ktor.utils)
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

tasks.withType<Javadoc> {
    exclude("**/*OuterClass*")
}
