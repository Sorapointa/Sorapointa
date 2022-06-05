plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
}

dependencies {
    api(project(":sorapointa-utils:sorapointa-utils-serialization"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:_")

    val remubulanVer = "0.1-SNAPSHOT"
    api("net.sandius.rembulan:rembulan-runtime:$remubulanVer")
    api("net.sandius.rembulan:rembulan-stdlib:$remubulanVer")
    api("net.sandius.rembulan:rembulan-compiler:$remubulanVer")
}
