/*
 * build.gradle.kts
 * KotlinTemplate
 */

/* -------------------------------------------------------------------------- */
// 🛃 Imports
/* -------------------------------------------------------------------------- */

import at.phatbl.shellexec.ShellExec
import org.gradle.api.JavaVersion.*
import java.util.Date
import org.gradle.internal.impldep.org.bouncycastle.crypto.tls.BulkCipherAlgorithm.idea
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.version
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.preprocessor.mkdirsOrFail
import org.junit.platform.console.options.Details
import org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns

/* -------------------------------------------------------------------------- */
// 🔌 Plugins
/* -------------------------------------------------------------------------- */

plugins {
    // Gradle built-in
    jacoco
    `java-gradle-plugin`
    maven // only applied to make bintray happy
    `maven-publish`

    // Gradle plugin portal - https://plugins.gradle.org/
    kotlin("jvm") version "1.2.31"
    id("at.phatbl.clamp") version "1.0.0"
    id("at.phatbl.shellexec") version "1.1.3"
    id("com.gradle.plugin-publish") version "0.9.10"
    id("com.jfrog.bintray") version "1.8.0"
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC6-4"

    // Custom handling in pluginManagement
    id("org.junit.platform.gradle.plugin") version "1.1.0"
}

/* -------------------------------------------------------------------------- */
// 📋 Properties
/* -------------------------------------------------------------------------- */

val artifactName by project
val javaPackage = "$group.$artifactName"
val pluginClass by project
val projectUrl by project
val tags by project
val labels = "$tags".split(",")
val license by project

val jvmTarget = JavaVersion.VERSION_1_8

val commonsExecVersion by project
val spekVersion by project
val detektVersion by project
val junitPlatformVersion by project
val jacocoVersion by project

/* -------------------------------------------------------------------------- */
// 👪 Dependencies
/* -------------------------------------------------------------------------- */

repositories.jcenter()

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("org.junit.platform:junit-platform-runner:$junitPlatformVersion")
    testImplementation("org.jetbrains.spek:spek-api:$spekVersion")
    testImplementation("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion")
}

/* -------------------------------------------------------------------------- */
// 🏗 Assemble
/* -------------------------------------------------------------------------- */

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "$jvmTarget" }

configure<JavaPluginConvention> {
    sourceCompatibility = jvmTarget
    targetCompatibility = jvmTarget
}

// Include resources
java.sourceSets["main"].resources {
    setSrcDirs(mutableListOf("src/main/resources"))
    include("VERSION.txt")
}

val updateVersionFile by tasks.creating {
    description = "Updates the VERSION.txt file included with the plugin"
    group = "Build"
    doLast {
        val resources = "src/main/resources"
        project.file(resources).mkdirsOrFail()
        val versionFile = project.file("$resources/VERSION.txt")
        versionFile.createNewFile()
        versionFile.writeText(version.toString())
    }
}

tasks.getByName("assemble").dependsOn(updateVersionFile)

val sourcesJar by tasks.creating(Jar::class) {
    dependsOn("classes")
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn("javadoc")
    classifier = "javadoc"
    val javadoc = tasks.withType<Javadoc>().first()
    from(javadoc.destinationDir)
}

artifacts.add("archives", sourcesJar)
artifacts.add("archives", javadocJar)

configure<BasePluginConvention> {
    // at.phatbl.shellexec-1.0.0.jar
    archivesBaseName = javaPackage
}

/* -------------------------------------------------------------------------- */
// ✅ Test
/* -------------------------------------------------------------------------- */

junitPlatform {
    filters {
        engines {
            include("spek")
        }
        includeClassNamePatterns("^.*Tests?$", ".*Spec", ".*Spek")
    }
    details = Details.TREE
}

tasks.withType<JacocoReport> {
    reports {
        sourceDirectories = fileTree("src/main/kotlin")
        classDirectories = fileTree("$buildDir/classes/kotlin/main")

        xml.apply {
            isEnabled = true
            destination = File("$buildDir/reports/jacoco.xml")
        }
        csv.apply {
            isEnabled = false
        }
        html.apply {
            isEnabled = true
            destination = File("$buildDir/jacocoHtml")
        }

        executionData(tasks.withType<Test>())
    }
}

val codeCoverageReport by tasks.creating(JacocoReport::class) {
    dependsOn("test")
    sourceSets(java.sourceSets["main"])
}

/* -------------------------------------------------------------------------- */
// 🔍 Code Quality
/* -------------------------------------------------------------------------- */

detekt {
    version = "$detektVersion"
    profile("main", Action {
        input = "$projectDir/src/main/kotlin"
        config = "$projectDir/detekt.yml"
        filters = ".*test.*,.*/resources/.*,.*/tmp/.*"
    })
    idea(Action {
        path = ".idea"
        codeStyleScheme = ".idea/code-style.xml"
        inspectionsProfile = ".idea/inspect.xml"
        report = "$projectDir/reports"
        mask = "*.kt,"
    })
}

val lint by tasks.creating(DefaultTask::class) {
    description = "Runs detekt and validateTaskProperties"
    group = "Verification"
    // Does this task come from java-gradle-plugin?
    dependsOn("validateTaskProperties")
    dependsOn("detektCheck")
}

val danger by tasks.creating(ShellExec::class) {
    description = "Runs danger rules."
    group = "Verification"
    command = """\
        bundle install --gemfile=Gemfile --verbose
        ./bin/danger --verbose"""
}

val codeQuality by tasks.creating(DefaultTask::class) {
    description = "Runs all code quality checks."
    group = "🚇 Tube"
    dependsOn("detektCheck")
    dependsOn("check")
    dependsOn(lint)
}

/* -------------------------------------------------------------------------- */
// 🔖 Release
/* -------------------------------------------------------------------------- */

val release by tasks.creating(DefaultTask::class) {
    description = "Performs release actions."
    group = "🚇 Tube"
    doLast { logger.lifecycle("Release task not implemented.") }
}

/* -------------------------------------------------------------------------- */
// 🚀 Deployment
/* -------------------------------------------------------------------------- */

publishing {
    (publications) {
        "mavenJava"(MavenPublication::class) {
            from(components["java"])
            artifactId = "$artifactName"

            artifact(sourcesJar) { classifier = "sources" }
            artifact(javadocJar) { classifier = "javadoc" }
        }
    }
}

bintray {
    user = property("bintray.user") as String
    key = property("bintray.api.key") as String
    setPublications("mavenJava")
    setConfigurations("archives")
    dryRun = false
    publish = true
    pkg.apply {
        repo = property("bintray.repo") as String
        name = project.name
        desc = project.description
        websiteUrl = "$projectUrl"
        issueTrackerUrl = "$projectUrl/issues"
        vcsUrl = "$projectUrl.git"
        githubRepo = "phatblat/${project.name}"
        githubReleaseNotesFile = "CHANGELOG.md"
        setLicenses(property("license") as String)
        setLabels("gradle", "plugin", "exec", "shell", "bash", "kotlin")
        publicDownloadNumbers = true
        version.apply {
            name = project.version.toString()
            desc = "ShellExec Gradle Plugin ${project.version}"
            released = Date().toString()
            vcsTag = "$project.version"
            attributes = mapOf("gradle-plugin" to "${project.group}:$artifactName:$version")

            mavenCentralSync.apply {
                sync = false //Optional (true by default). Determines whether to sync the version to Maven Central.
                user = "userToken" //OSS user token
                password = "password" //OSS user password
                close = "1" //Optional property. By default the staging repository is closed and artifacts are released to Maven Central. You can optionally turn this behaviour off (by puting 0 as value) and release the version manually.
            }
        }
    }
}

// Workaround to eliminate warning from bintray plugin, which assumes the "maven" plugin is being used.
// https://github.com/bintray/gradle-bintray-plugin/blob/master/src/main/groovy/com/jfrog/bintray/gradle/BintrayPlugin.groovy#L85
val install by tasks
install.doFirst {
    val maven = project.convention.plugins["maven"] as MavenPluginConvention
    maven.mavenPomDir = file("$buildDir/publications/mavenJava")
    logger.info("Configured maven plugin to use same output dir as maven-publish: ${maven.mavenPomDir}")
}

val deploy by tasks.creating {
    description = "Deploys the artifact."
    group = "Deployment"
    dependsOn("bintrayUpload")
    dependsOn("publishPlugins")
}
