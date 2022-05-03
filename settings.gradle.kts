rootProject.name = "Sorapointa"

include("sorapointa-core")
include("sorapointa-dataprovider")
include("sorapointa-dispatch")
include("sorapointa-i18n")
include("sorapointa-kcp")
include("sorapointa-proto")
include("sorapointa-utils")
include("sorapointa-command")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.40.1"
}
