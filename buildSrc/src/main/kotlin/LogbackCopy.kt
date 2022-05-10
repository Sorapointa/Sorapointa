import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.register

fun Project.configureLogbackCopy() {
    fun registerCopyPath(name: String, source: String, dest: String) {
        tasks.register(name, Copy::class) {
            from(
                rootProject.subprojects.first { it.name == "sorapointa-core" }
                    .layout.projectDirectory.dir(source)
            )
            into(project.layout.projectDirectory.dir(dest))
        }
    }

    registerCopyPath(
        name = "copyLogbackXml",
        source = "./src/main/resources/logback.xml",
        dest = "./src/main/resources/"
    )
    registerCopyPath(
        name = "copyLogbackTestXml",
        source = "./src/test/resources/logback-test.xml",
        dest = "./src/test/resources/"
    )

    afterEvaluate {
        tasks.named("classes") {
            dependsOn("copyLogbackXml")
        }

        tasks.named("testClasses") {
            dependsOn("copyLogbackTestXml")
        }

        tasks.named("processResources") {
            dependsOn("copyLogbackXml")
        }

        tasks.named("processTestResources") {
            dependsOn("copyLogbackTestXml")
        }
    }
}

private val excludes = listOf("sorapointa-core", "buildSrc")

fun Collection<Project>.configureLogbackCopy() =
    filter { !excludes.contains(it.name) }
        .forEach { it.configureLogbackCopy() }
