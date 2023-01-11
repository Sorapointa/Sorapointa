import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.register

internal fun Project.resourceTaskDep(task: String) {
    tasks.named("classes") {
        dependsOn(task)
    }

    tasks.named("processResources") {
        dependsOn(task)
    }
}

internal fun Project.testResourceTaskDep(task: String) {
    tasks.named("testClasses") {
        dependsOn(task)
    }

    tasks.named("processTestResources") {
        dependsOn(task)
    }
}

fun Project.configureLogbackCopy() {
    if (!pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")) return
    if (name == "sorapointa-core") return

    fun registerCopyPath(name: String, source: String, dest: String) {
        tasks.register(name, Copy::class) {
            group = "resources"
            from(
                rootProject.subprojects.first { it.name == "sorapointa-core" }
                    .layout.projectDirectory.dir(source),
            )
            into(project.layout.projectDirectory.dir(dest))
        }
    }

    registerCopyPath(
        name = "copyLogbackXml",
        source = "./src/main/resources/logback.xml",
        dest = "./src/main/resources/",
    )
    registerCopyPath(
        name = "copyLogbackTestXml",
        source = "./src/test/resources/logback-test.xml",
        dest = "./src/test/resources/",
    )

    afterEvaluate {
        resourceTaskDep("copyLogbackXml")
        testResourceTaskDep("copyLogbackTestXml")
    }
}
