rootProject.name = "Sorapointa"

include("sorapointa-kcp")
include("sorapointa-dispatch")
include("sorapointa-core")
include("sorapointa-utils")
include("sorapointa-dataprovider")
include("sorapointa-proto")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.40.1"
}
