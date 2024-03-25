@file:Suppress("SpellCheckingInspection", "GradleDependency", "AndroidGradlePluginVersion")

/*
 * This file configures the build system that creates your Android app.
 * The syntax is Kotlin, not Java.
 * You do not need to understand the contents of this file, nor should you modify it.
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 */

plugins {
    id("com.android.application") version "8.3.0" apply false
    id("com.diffplug.spotless") version "6.25.0"
    java
}
spotless {
    java {
        googleJavaFormat("1.21.0")
        target("app/src/*/java/**/*.java")
    }
    kotlinGradle {
        ktlint("1.2.1")
        target("**/*.gradle.kts")
    }
}
