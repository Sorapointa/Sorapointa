plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    kotlin("plugin.serialization")
}

dependencies {
    api(KotlinX.serialization.json)
    api("com.charleskorn.kaml:kaml:_")
}
