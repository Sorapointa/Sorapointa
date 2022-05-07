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

repositories {
    maven("https://plugins.gradle.org/m2/")
}

version = "0.1.0-Dev"

dependencies {
    api("com.google.protobuf:protobuf-java:_")
    api("com.google.protobuf:protobuf-kotlin:_")
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
