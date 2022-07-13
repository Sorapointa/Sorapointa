import org.jlleitschuh.gradle.ktlint.KtlintExtension
import java.nio.file.Files

plugins {
    kotlin("plugin.serialization") apply false
    kotlin("jvm") apply false
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    afterEvaluate {
        configureLogbackCopy()
    }
    configure<KtlintExtension> {
        disabledRules.set(setOf("no-wildcard-imports", "import-ordering"))
        filter {
            // exclude("**/generated/**")
            fun exclude(path: String) = exclude {
                projectDir.toURI().relativize(it.file.toURI()).normalize().path.contains(path)
            }
            setOf("/generated/", "/build/", "resources").forEach { exclude(it) }
        }
    }
}

allprojects {
    repositories {
        maven("https://maven.aliyun.com/repository/public")
    }
    group = "moe.sdl.sorapointa"
    version = "0.1.0-SNAPSHOT"
}

fun installGitHooks() {
    val target = File(project.rootProject.rootDir, ".git/hooks")
    val source = File(project.rootProject.rootDir, ".git-hooks")
    if (target.canonicalFile == source) return
    target.deleteRecursively()
    Files.createSymbolicLink(target.toPath(), source.toPath())
}
installGitHooks()
