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
