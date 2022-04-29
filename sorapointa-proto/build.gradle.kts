import com.google.protobuf.gradle.*

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
    protobuf(files("./proto/"))
    testProtobuf(files("./proto/"))
}


protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:_"
    }
    // generatedFilesBaseDir = "$projectDir/src/main/java/emu/grasscutter/net/proto/"
    generatedFilesBaseDir = "$projectDir/src/generated/"
}


sourceSets {
    main {
        proto {
            // In addition to the default 'src/main/proto'
            srcDir("src/generated")
        }
        java {
            srcDir("src/kotlin")
        }
    }
}

idea {
    module {
        sourceDirs.plusAssign(file("./proto/"))
    }
}
//
//processResources {
//    dependsOn("generateProto")
//}
