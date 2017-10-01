import org.gradle.api.plugins.ExtensionAware
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.embeddedKotlinVersion
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import org.junit.platform.gradle.plugin.FiltersExtension
import org.junit.platform.gradle.plugin.EnginesExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

//val kotlin_version:String by extra

// setup the plugin
buildscript {
    extra["kotlin_version"] = "1.1.50"
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0")
    }
}

apply {
    plugin("org.junit.platform.gradle.plugin")
}


plugins {
    kotlin("jvm", "1.1.50")
    application
}

application {
    mainClassName = "org.kruste.todo.asynchronous.MainKt"
}

// extension for configuration
fun JUnitPlatformExtension.filters(setup: FiltersExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(FiltersExtension::class.java).setup()
        else -> throw Exception("${this::class} must be an instance of ExtensionAware")
    }
}

fun FiltersExtension.engines(setup: EnginesExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(EnginesExtension::class.java).setup()
        else -> throw Exception("${this::class} must be an instance of ExtensionAware")
    }
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
    compile(kotlin("stdlib", "1.1.50"))
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines", version = "0.18")
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "0.18")
    compile(group = "io.vavr", name = "vavr-kotlin", version = "0.9.1")

    testCompile(group = "org.jetbrains.kotlin", name = "kotlin-test")
    runtime(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "1.1.50")
    testCompile(group = "org.jetbrains.spek", name = "spek-api", version = "1.1.5") {
        exclude("org.jetbrains.kotlin")
    }
    testRuntime(group = "org.jetbrains.spek", name = "spek-junit-platform-engine", version = "1.1.5") {
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


