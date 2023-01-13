plugins {
    `sorapointa-conventions`
    id("fr.stardustenterprises.rust.importer")
}

dependencies {
    implementation(libs.bundles.log)
    implementation(project(":sorapointa-utils:sorapointa-utils-core"))
    implementation(libs.yanl)
    implementation(libs.plat4k)
    testImplementation(libs.kotlinx.coroutines.core)
    rust(project(":sorapointa-native"))
}

repositories {
    mavenCentral()
}

rustImport {
    baseDir.set("/org/sorapointa/rust")
    layout.set("hierarchical")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }
}

jniHeaderTask(tasks)
