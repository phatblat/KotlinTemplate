/*
 * build.gradld.kts
 * Spek-1.1.5
 */

val junitPlatformVersion = "1.0.0"
val spekVersion          = "1.1.5"

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.2.30"
    id("org.junit.platform.gradle.plugin") version "1.0.0"
}

repositories {
    jcenter()
}

dependencies {
    // JUnit Platform
    testImplementation("org.junit.platform:junit-platform-runner:$junitPlatformVersion")
    // Spek
    testImplementation("org.jetbrains.spek:spek-api:$spekVersion")
    testImplementation("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion")
}
