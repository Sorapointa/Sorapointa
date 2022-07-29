plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
    kotlin("plugin.serialization")
}

dependencies {
    project.parent!!.subprojects.filter {
        it.name != "sorapointa-utils-all"
    }.forEach {
        api(it)
    }
}
