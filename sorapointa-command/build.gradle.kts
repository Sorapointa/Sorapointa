plugins {
    id("sorapointa-conventions")
}

version = "0.1.0-Dev"

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":sorapointa-i18n"))
    implementation(project(":sorapointa-utils"))
    implementation("moe.sdl.yac:core:+")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}
