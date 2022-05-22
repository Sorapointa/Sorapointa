@file:Suppress("GradlePackageUpdate")

import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.proto
import com.google.protobuf.gradle.protobuf

plugins {
    id("sorapointa-conventions")
    id("com.google.protobuf")
    idea
}

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
