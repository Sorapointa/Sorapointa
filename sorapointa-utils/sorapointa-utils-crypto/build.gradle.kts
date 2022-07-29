plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
}

dependencies {
    api(project(":sorapointa-utils:sorapointa-utils-core"))
    implementation("io.ktor:ktor-utils:_")
}
