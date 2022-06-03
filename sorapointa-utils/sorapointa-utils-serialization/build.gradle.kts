plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    kotlin("plugin.serialization")
}

dependencies {
    api(KotlinX.serialization.json)
    api("net.mamoe.yamlkt:yamlkt:_")
}
