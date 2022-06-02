plugins {
    `sorapointa-conventions`
    `sorapointa-publish`
}

dependencies {
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
    packageName("org.sorapointa.config")
    useKotlinOutput { topLevelConstants = true }
    string("VERSION", version.toString())
    string("BUILD_BRANCH", branch)
    string("COMMIT_HASH", commitHash)

    boolean("IS_CI", isCI)

    sourceSets["test"].apply {
        packageName("org.sorapointa.config")
        className("TestConfig")
        useKotlinOutput { topLevelConstants = false }
        useKotlinOutput()
        string("TEST_DIR", rootProject.rootDir.absolutePath.replace("\\", "/"))
    }
}
