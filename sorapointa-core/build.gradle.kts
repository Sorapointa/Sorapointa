plugins {
    id("sorapointa-conventions")
    application
}

version = "0.1.0-Dev"

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(project(":sorapointa-dispatch"))
    implementation(project(":sorapointa-i18n"))
    implementation(project(":sorapointa-kcp"))
    implementation(project(":sorapointa-proto"))
    implementation(project(":sorapointa-utils"))
    implementation("moe.sdl.yac:core:+")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

