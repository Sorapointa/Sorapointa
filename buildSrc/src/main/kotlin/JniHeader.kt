import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import java.io.ByteArrayOutputStream

val bodyExtractingRegex = Regex("""^.+\Rpublic \w* ?class ([^\s]+).*\{\R((?s:.+))\}\R$""")
val nativeMethodExtractingRegex = Regex(""".*\bnative\b.*""")

fun Project.jniHeaderTask(tasks: TaskContainer) = tasks.create("generateJniHeaders") {
    group = "build"
    dependsOn(tasks.getByName("compileKotlin"))

    project.kotlinExtension.sourceSets.getByName("main").kotlin.srcDirs.filter {
        it.exists()
    }.forEach {
        inputs.dir(it)
    }
    outputs.dir("src/main/generated/jni")

    doLast {
        val javaHome = org.gradle.internal.jvm.Jvm.current().javaHome
        val javap = javaHome.resolve("bin").walk()
            .firstOrNull { it.name.startsWith("javap") }
            ?.absolutePath ?: error("javap not found")
        val javac = javaHome.resolve("bin").walk()
            .firstOrNull { it.name.startsWith("javac") }
            ?.absolutePath ?: error("javac not found")
        val buildDir = file("build/classes/kotlin/main")
        val tmpDir = file("build/tmp/jvmJni")
        tmpDir.mkdirs()

        buildDir.walk()
            .asSequence()
            .filter { "META" !in it.absolutePath }
            .filter { it.isFile }
            .filter { it.extension == "class" }
            .forEach { file ->
                val output = ByteArrayOutputStream().use {
                    project.exec {
                        commandLine(javap, "-private", "-cp", buildDir.absolutePath, file.absolutePath)
                        standardOutput = it
                    }.assertNormalExitValue()
                    it.toString()
                }

                val (qualifiedName, methodInfo) =
                    bodyExtractingRegex
                        .find(output)?.destructured
                        ?: return@forEach

                val lastDot = qualifiedName.lastIndexOf('.')
                val packageName = qualifiedName.substring(0, lastDot)
                val className = qualifiedName.substring(lastDot + 1, qualifiedName.length)

                val nativeMethods =
                    nativeMethodExtractingRegex.findAll(methodInfo).map { it.groups }
                        .flatMap { it.asSequence().mapNotNull { group -> group?.value } }.toList()
                if (nativeMethods.isEmpty()) return@forEach

                val generatedCode = buildString {
                    appendLine("package $packageName;")
                    appendLine("public class $className {")
                    nativeMethods.forEach { method ->
                        val newMethod = if (method.contains("()")) {
                            method
                        } else {
                            buildString {
                                append(method)
                                var count = 0
                                var i = 0
                                while (i < length) {
                                    if (this[i] == ',' || this[i] == ')') {
                                        count++
                                        insert(i, " arg$count".also { i += it.length + 1 })
                                    } else {
                                        i++
                                    }
                                }
                            }
                        }
                        appendLine(newMethod)
                    }
                    appendLine("}")
                }
                val javaFile = tmpDir
                    .resolve(packageName.replace(".", "/"))
                    .resolve("$className.java")
                javaFile.parentFile.mkdirs()
                if (javaFile.exists()) delete()
                javaFile.createNewFile()
                javaFile.writeText(generatedCode)
                project.exec {
                    commandLine(javac, "-h", "src/main/generated/jni", javaFile.absolutePath)
                }.assertNormalExitValue()
            }
    }
}
