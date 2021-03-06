plugins {
    `sorapointa-conventions`
    kotlin("plugin.serialization")
}

dependencies {
    api(KotlinX.serialization.json)
    api(KotlinX.datetime)
    implementation("io.ktor:ktor-utils:_")
}

val commitHash by lazy {
    val commitHashCommand = "git rev-parse --short HEAD"
    Runtime.getRuntime().exec(commitHashCommand).inputStream.bufferedReader().readLine() ?: "UnkCommit"
}

val branch by lazy {
    val branchCommand = "git rev-parse --abbrev-ref HEAD"
    Runtime.getRuntime().exec(branchCommand).inputStream.bufferedReader().readLine() ?: "UnkBranch"
}

buildConfig {
    packageName("$group.config")
    useKotlinOutput { topLevelConstants = true }
    string("VERSION", version.toString())
    string("BUILD_BRANCH", branch)
    string("COMMIT_HASH", commitHash)

    boolean("IS_CI", isCI)

    sourceSets["test"].apply {
        packageName("$group.config")
        className("TestConfig")
        useKotlinOutput { topLevelConstants = false }
        useKotlinOutput()
        string("TEST_DIR", rootProject.rootDir.absolutePath.replace("\\", "/"))
    }
}
