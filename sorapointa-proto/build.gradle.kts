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
    implementation("com.google.protobuf:protobuf-java:_")
}


protobuf {
    generatedFilesBaseDir = "$projectDir/src/generated/"
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
    test {
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
