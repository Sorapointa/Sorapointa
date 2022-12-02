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
    implementation(project(":sorapointa-utils:sorapointa-utils-serialization"))

    implementation("org.jetbrains.kotlin:kotlin-reflect:_")

    implementation("org.jetbrains.kotlinx:atomicfu:_")
    // Database
    api("org.jetbrains.exposed:exposed-core:_")
    api("org.jetbrains.exposed:exposed-dao:_")
    api("org.jetbrains.exposed:exposed-jdbc:_")
    api("org.jetbrains.exposed:exposed-kotlin-datetime:_")
    implementation("com.zaxxer:HikariCP:_")

    val databaseCompileList =
        databaseCompileList?.split(',')
            ?.map { it.toLowerCaseAsciiOnly() }
            ?.map { it.trim() }
            ?: listOf("sqlite")

    if (databaseCompileList.contains("sqlite")) {
        implementation("org.xerial:sqlite-jdbc:_")
    }

    implementation("org.postgresql:postgresql:_")
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
