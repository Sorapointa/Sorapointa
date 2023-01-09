plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
}

dependencies {
    api(project(":sorapointa-utils:sorapointa-utils-core"))
    implementation(libs.ktor.utils)
}
