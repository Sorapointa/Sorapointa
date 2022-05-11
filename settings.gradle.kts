rootProject.name = "Sorapointa"

include("sorapointa-core")
include("sorapointa-dataprovider")
include("sorapointa-dispatch")
include("sorapointa-i18n")
include("sorapointa-event")
include("sorapointa-kcp")
include("sorapointa-proto")
include("sorapointa-utils")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.40.1"
}
include("sorapointa-dataloader")
