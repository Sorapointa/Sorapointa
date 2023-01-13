rootProject.name = "Sorapointa"

include("sorapointa-core")
include("sorapointa-crypto")
include("sorapointa-dataloader")
include("sorapointa-dataprovider")
include("sorapointa-dispatch")
include("sorapointa-event")
include("sorapointa-i18n")
include("sorapointa-native")
include("sorapointa-native-wrapper")
include("sorapointa-proto")
include("sorapointa-task")

include("sorapointa-utils")
include("sorapointa-utils:sorapointa-utils-all")
include("sorapointa-utils:sorapointa-utils-core")
include("sorapointa-utils:sorapointa-utils-crypto")
include("sorapointa-utils:sorapointa-utils-serialization")
include("sorapointa-utils:sorapointa-utils-time")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}
