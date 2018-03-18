/*
 * build.gradld.kts
 * Spek-1.1.5
 */

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.2.30"
    id("org.junit.platform.gradle.plugin") version "1.0.0"
}

val spekVersion          = "1.1.5"

// This is necessary to make the plugin version accessible in other places
// https://stackoverflow.com/questions/46053522/how-to-get-ext-variables-into-plugins-block-in-build-gradle-kts/47507441#47507441
val junitPlatformVersion: String? by extra {
    buildscript.configurations["classpath"]
            .resolvedConfiguration.firstLevelModuleDependencies
            .find { it.moduleName == "junit-platform-gradle-plugin" }?.moduleVersion
}

repositories {
    jcenter()
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    // JUnit Platform
    testImplementation("org.junit.platform:junit-platform-runner:$junitPlatformVersion")
    // Spek
    testImplementation("org.jetbrains.spek:spek-api:$spekVersion")
    testImplementation("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion")
}
