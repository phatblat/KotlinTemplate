/*
 * settings.gradle.kts
 * Spek-1.1.5
 */

rootProject.name = "Spek-1.1.5"

// Workaround to make the JUnit Platform Gradle Plugin available using the `plugins` DSL
    // https://github.com/junit-team/junit5/issues/768#issuecomment-330078905
pluginManagement {
    repositories {
        gradlePluginPortal()
        // jcenter()
        // maven { url = uri("") }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.junit.platform.gradle.plugin") {
                useModule("org.junit.platform:junit-platform-gradle-plugin:${requested.version}")
            }
        }
    }
}
