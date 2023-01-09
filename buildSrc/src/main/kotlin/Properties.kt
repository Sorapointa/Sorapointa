import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.util.*

fun Project.getRootProjectLocalProps(): Map<String, String> {
    val file = project.rootProject.file("local.properties")
    return if (file.exists()) {
        file.reader().use {
            Properties().apply {
                load(it)
            }
        }.toMap().map {
            it.key.toString() to it.value.toString()
        }.toMap()
    } else emptyMap()
}



fun Project.getExtraString(name: String) = runCatching { this.extra[name]?.toString() }.getOrNull()

fun Project.getExtraBoolean(name: String) = runCatching { this.extra[name] as Boolean }.getOrNull()
