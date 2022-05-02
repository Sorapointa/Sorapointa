plugins {
    id("sorapointa-conventions")
    application
}

version = "0.1.0-Dev"

dependencies {
    implementation(project(":sorapointa-dataprovider"))
    implementation(project(":sorapointa-dispatch"))
    implementation(project(":sorapointa-event"))
    implementation(project(":sorapointa-i18n"))
    implementation(project(":sorapointa-kcp"))
    implementation(project(":sorapointa-proto"))
    implementation(project(":sorapointa-utils"))
}
