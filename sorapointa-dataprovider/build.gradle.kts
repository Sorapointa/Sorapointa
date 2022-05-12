import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

plugins {
    id("sorapointa-conventions")
    id("com.github.gmazzo.buildconfig")
}

val props = getRootProjectLocalProps()

val defaultDatabaseType: String? = props["database.default"]

val databaseCompileList: String? = props["database.driver.list"]

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:_")

    implementation(KotlinX.serialization.json)
    implementation("org.jetbrains.kotlinx:atomicfu:_")
    // Database
    api("org.jetbrains.exposed:exposed-core:_")
    api("org.jetbrains.exposed:exposed-dao:_")
    api("org.jetbrains.exposed:exposed-jdbc:_")
    api("org.jetbrains.exposed:exposed-kotlin-datetime:_")
    implementation("com.zaxxer:HikariCP:3.4.2")

    val databaseCompileList =
        databaseCompileList?.split(',')
            ?.map { it.toLowerCaseAsciiOnly() }
            ?.map { it.trim() }
            ?: listOf("sqlite")

    if (databaseCompileList.contains("sqlite")) {
        implementation("org.xerial:sqlite-jdbc:_")
    }

    if (databaseCompileList.contains("postgresql")) {
        implementation("org.postgresql:postgresql:_")
    }
}

fun BuildConfigSourceSet.dbType(name: String, value: String) =
    buildConfigField("org.sorapointa.data.provider.DatabaseType", name, "DatabaseType.$value")

buildConfig {
    packageName("$group.config")
    useKotlinOutput { topLevelConstants = true }
    dbType("DEFAULT_DATABASE_TYPE", (defaultDatabaseType ?: databaseCompileList?.firstOrNull() ?: "SQLITE").toString())

    sourceSets["test"].apply {
        if (!isCI) {
            val provider: String = props["database.test.provider"] ?: "SQLITE"
            val file = File(rootProject.projectDir, "tmp/test.db").apply {
                parentFile?.mkdirs()
            }
            val connection: String = props["database.test.connection"]
                ?: "jdbc:sqlite:${file.absolutePath}"

            dbType("TEST_DATABASE_PROVIDER", provider)

            string("TEST_CONNECTION", connection)
        }
    }
}
