rootProject.name = "Sorapointa"

include("sorapointa-core")
include("sorapointa-dataloader")
include("sorapointa-dataprovider")
include("sorapointa-dispatch")
include("sorapointa-event")
include("sorapointa-i18n")
include("sorapointa-kcp")
include("sorapointa-proto")
include("sorapointa-task")
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
