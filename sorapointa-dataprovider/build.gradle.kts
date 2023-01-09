@file:Suppress("GradlePackageUpdate")

import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
}

val props = getRootProjectLocalProps()

val defaultDatabaseType: String? = props["database.default"]

val databaseCompileList: String? = props["database.driver.list"]

dependencies {
    implementation(project(":sorapointa-utils:sorapointa-utils-core"))
    implementation(project(":sorapointa-utils:sorapointa-utils-serialization"))

    implementation(libs.bundles.log)
    implementation(libs.kotlin.reflect)
    implementation(libs.atomicfu)

    // Database
    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.exposed.kotlin.datetime)
    implementation(libs.hikaricp)

    val databaseCompileList =
        databaseCompileList?.split(',')
            ?.map { it.toLowerCaseAsciiOnly() }
            ?.map { it.trim() }
            ?: listOf("sqlite")

    if (databaseCompileList.contains("sqlite")) {
        implementation(libs.sqlite)
    }

    implementation(libs.postgresql)
}

fun BuildConfigSourceSet.dbType(name: String, value: String) =
    buildConfigField("org.sorapointa.data.provider.DatabaseType", name, "DatabaseType.$value")

buildConfig {
    packageName("org.sorapointa.config")
    className("DbMeta")
    useKotlinOutput()
    dbType(
        name = "DEFAULT_DATABASE_TYPE",
        value = (defaultDatabaseType ?: databaseCompileList?.firstOrNull() ?: "SQLITE").toString()
    )
}
