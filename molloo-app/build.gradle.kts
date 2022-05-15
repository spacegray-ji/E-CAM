// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        // This is NOT repo for project!!
        // Use settings.gradle.kts instead.
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    }
}

tasks.register("clean",Delete::class) {
    delete(rootProject.buildDir)
}