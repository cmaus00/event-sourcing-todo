import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.Project

// setup the plugin
buildscript {
    project.run {
        extra["kotlinVersion"] = "1.1.51"
        extra["kotlinCoroutinesVersion"] = "0.18"
        extra["vavrKotlinVersion"] = "0.9.1"
        extra["spekVersion"] = "1.1.5"
    }
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0")
    }
}

val kotlinVersion: String by extra
val kotlinCoroutinesVersion: String by extra
val vavrKotlinVersion: String by extra
val spekVersion: String by extra


apply {
    plugin("org.junit.platform.gradle.plugin")
}


plugins {
    kotlin("jvm")
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
    maven("http://dl.bintray.com/jetbrains/spek")
    maven("http://dl.bintray.com/kotlin/kotlinx")
}

dependencies {
    compile(kotlin("stdlib", kotlinVersion))
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines", version = kotlinCoroutinesVersion)
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = kotlinCoroutinesVersion)
    compile(group = "io.vavr", name = "vavr-kotlin", version = vavrKotlinVersion)

    testCompile(group = "org.jetbrains.kotlin", name = "kotlin-test")
    runtime(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = kotlinVersion)
    testCompile(group = "org.jetbrains.spek", name = "spek-api", version = spekVersion) {
        exclude("org.jetbrains.kotlin")
    }
    testRuntime(group = "org.jetbrains.spek", name = "spek-junit-platform-engine", version = spekVersion) {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.junit.platform")
    }
}

val project = mapOf(
        name to "todo"
)


val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
    manifest {
        attributes["Main-Class"] = "todo.MainKt"
    }
    from(configurations.runtime.map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks["jar"] as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}


