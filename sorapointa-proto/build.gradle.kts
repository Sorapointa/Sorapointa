@file:Suppress("GradlePackageUpdate")

plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    id("com.squareup.wire")
    idea
}

val prop = getRootProjectLocalProps()

dependencies {
    implementation(project(":sorapointa-utils:sorapointa-utils-core"))
    api(libs.wire.runtime)
    api(libs.ktor.utils)
}

wire {
    sourcePath {
        srcDir("src/proto")
    }
    kotlin {
        rpcRole = "none"
        rpcCallStyle = "suspending"
    }
}

idea {
    module {
        sourceDirs.add(project.file("src/proto"))
    }
}

tasks.withType<Javadoc> {
    exclude("**/*OuterClass*")
}
