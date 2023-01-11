import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig")
    id("com.github.johnrengelman.shadow")
    java
}

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation(kotlin("test"))
}

sourceSets {
    main {
        java {
            setSrcDirs(setOf("kotlin")) // No Java, and Kotlin Only
        }
    }
    test {
        java {
            setSrcDirs(setOf("kotlin")) // No Java, and Kotlin Only
        }
    }
}

tasks.test {
    dependsOn("generateTestBuildConfig")
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.apply {
        jvmTarget = "17"
        OptInAnnotations.list.forEach {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=$it"
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

configurations {
    create("test")
}

tasks.register<Jar>("testArchive") {
    archiveBaseName.set("${project.name}-test")
    from(project.the<SourceSetContainer>()["test"].output)
}

artifacts {
    add("test", tasks["testArchive"])
}

tasks.withType<Jar>() {
    exclude("main") // duplicated jar root main, very confusing
    exclude("logback-test.xml")
    exclude("*.proto")
}

tasks.shadowJar {
    exclude("checkstyle.xml")
    exclude("**/*.html")
    exclude("CronUtilsI18N*.properties")
    exclude("DebugProbesKt.bin")
    exclude("custom.config.*")

    // SQLite
    exclude("org/sqlite/native/FreeBSD/**/*")
    exclude("org/sqlite/native/Linux-Android/**/*")
    exclude("org/sqlite/native/Linux-Musl/**/*")
    arrayOf("arm", "armv6", "armv7", "ppc64", "x86").forEach {
        exclude("org/sqlite/native/Linux/$it/**/*")
        exclude("org/sqlite/native/Windows/$it/**/*")
    }
    arrayOf("freebsd32", "freebsd64", "linux32", "windows32").forEach {
        exclude("META-INF/native/$it/**/*")
    }

    // JNA
    arrayOf("aix", "freebsd", "openbsd", "sunos").forEach {
        exclude("com/sun/jna/$it*/**/*")
    }
    arrayOf("arm", "armel", "loongarch64", "mips64el", "ppc", "ppc64le", "riscv64", "s390x", "x86").forEach {
        exclude("com/sun/jna/linux-$it/**/*")
        exclude("com/sun/jna/win32-$it/**/*")
    }

    // Jansi Native Lib
    exclude("org/fusesource/jansi/internal/native/FreeBSD")
    arrayOf("arm", "armv6", "armv7", "ppc64", "x86").forEach {
        exclude("org/fusesource/jansi/internal/native/Linux/$it/**/*")
        exclude("org/fusesource/jansi/internal/native/Mac/$it/**/*")
        exclude("org/fusesource/jansi/internal/native/Windows/$it/**/*")
    }
}
