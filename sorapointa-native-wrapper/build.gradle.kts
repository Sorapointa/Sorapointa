plugins {
    `sorapointa-conventions`
    id("fr.stardustenterprises.rust.importer")
}

dependencies {
    implementation(libs.bundles.log)
    implementation(libs.yanl)
    implementation(libs.plat4k)
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
