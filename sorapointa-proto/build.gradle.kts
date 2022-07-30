@file:Suppress("GradlePackageUpdate")

import com.google.protobuf.gradle.*
import de.fayard.refreshVersions.core.versionFor

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

    val protocVersion = versionFor("version.com.google.protobuf..protoc")
    try {
        """(\d)\.(\d+)\.(\d)""".toRegex().find(
            Runtime.getRuntime().exec("protoc --version").inputReader().readLine()
        )?.groupValues?.let {
            val protocVersionSplit = protocVersion.split('.')
            if (it[1].toInt() == protocVersionSplit[0].toInt() &&
                it[2].toInt() >= protocVersionSplit[1].toInt()
            ) return@let
            else throw Exception("Inappropriate version of protoc in PATH.")
        }
    } catch (e: Exception) {
        logger.warn("Problem with protoc in PATH: $e")
        protoc {
            artifact = "com.google.protobuf:protoc:$protocVersion"
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
